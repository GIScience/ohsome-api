package org.heigit.ohsome.ohsomeapi.executor;

import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.heigit.ohsome.ohsomeapi.Application;
import org.heigit.ohsome.ohsomeapi.controller.dataextraction.elements.ElementsGeometry;
import org.heigit.ohsome.ohsomeapi.exception.BadRequestException;
import org.heigit.ohsome.ohsomeapi.exception.ExceptionMessages;
import org.heigit.ohsome.ohsomeapi.inputprocessing.InputProcessingUtils;
import org.heigit.ohsome.ohsomeapi.inputprocessing.InputProcessor;
import org.heigit.ohsome.ohsomeapi.inputprocessing.SimpleFeatureType;
import org.heigit.ohsome.ohsomeapi.oshdb.DbConnData;
import org.heigit.ohsome.ohsomeapi.output.Attribution;
import org.heigit.ohsome.ohsomeapi.output.ExtractionResponse;
import org.heigit.ohsome.ohsomeapi.output.Metadata;
import org.heigit.ohsome.ohsomeapi.refactoring.operations.ContributionView;
import org.heigit.ohsome.ohsomeapi.refactoring.operations.Latest;
import org.heigit.ohsome.ohsomeapi.refactoring.operations.Operation;
import org.heigit.ohsome.ohsomeapi.refactoring.operations.SnapshotView;
import org.heigit.ohsome.ohsomeapi.refactoring.operations.extraction.ContributionsExtraction;
import org.heigit.ohsome.oshdb.api.db.OSHDBIgnite;
import org.heigit.ohsome.oshdb.api.db.OSHDBIgnite.ComputeMode;
import org.heigit.ohsome.oshdb.api.mapreducer.MapReducer;
import org.heigit.ohsome.oshdb.filter.FilterExpression;
import org.heigit.ohsome.oshdb.util.mappable.OSMContribution;
import org.heigit.ohsome.oshdb.util.mappable.OSMEntitySnapshot;
import org.heigit.ohsome.oshdb.util.tagtranslator.TagTranslator;
import org.heigit.ohsome.oshdb.util.time.IsoDateTimeParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.wololo.geojson.Feature;

/** Holds executor methods for the following endpoints: /elementsFullHistory, /contributions. */
@Component
public class DataRequestExecutor {

//  protected static final String URL = ExtractMetadata.attributionUrl;
//  protected static final String TEXT = ExtractMetadata.attributionShort;
  @Autowired
  Attribution attribution;
  public static final DecimalFormat df = ExecutionUtils.defineDecimalFormat("#.##");
  @Autowired
  InputProcessor inputProcessor;
  @Autowired
  InputProcessor snapshotInputProcessor;
  //private final ProcessingData processingData;
  //private final ElementsGeometry elementsGeometry;
  @Autowired
  HttpServletRequest servletRequest;
  @Autowired
  HttpServletResponse servletResponse;
  @Autowired
  InputProcessingUtils utils;
  @Autowired
  SnapshotView snapshotView;
  @Autowired
  ContributionView contributionView;

//  public DataRequestExecutor(RequestResource requestResource, ElementsGeometry elementsGeometry) {
//    this.requestResource = requestResource;
//    this.elementsGeometry = elementsGeometry;
//    //inputProcessor = new InputProcessor(servletRequest, false, false);
//    //processingData = inputProcessor.getProcessingData();
//  }

//  public void setInputProcessorFields() {
//System.out.println(inputProcessor.COMPUTE_MODE_THRESHOLD);
    //    inputProcessor.create();
//    inputProcessor.setSnapshot(false);
//    inputProcessor.setDensity(false);
 // }

  /**
   * Performs an OSM data extraction using the full-history of the data.
   *
   * @throws Exception thrown by {@link org.heigit.ohsome.ohsomeapi.inputprocessing.InputProcessor
   *         #processParameters() processParameters},
   *         {@link org.heigit.ohsome.oshdb.util.time.IsoDateTimeParser
   *         #parseIsoDateTime(String) parseIsoDateTime},
   *         {@link org.heigit.ohsome.oshdb.api.mapreducer.MapReducer#stream() stream}, or
   *         {@link org.heigit.ohsome.ohsomeapi.executor.ExecutionUtils
   *         #streamResponse(HttpServletResponse, ExtractionResponse, Stream)
   *         streamElementsResponse}
   */
  public void extract(Operation operation, ElementsGeometry elementsGeometry) throws Exception {
    //setInputProcessorFields();
    snapshotInputProcessor.getProcessingData().setFullHistory(true);
    snapshotInputProcessor.setSnapshot(true);
    snapshotInputProcessor.setDensity(false);
    //InputProcessor snapshotInputProcessor = new InputProcessor(servletRequest, true, false);
    snapshotInputProcessor.getProcessingData().setFullHistory(true);
    MapReducer<OSMEntitySnapshot> mapRedSnapshot = null;
    MapReducer<OSMContribution> mapRedContribution = null;
    if (DbConnData.db instanceof OSHDBIgnite) {
      // on ignite: Use AffinityCall backend, which is the only one properly supporting streaming
      // of result data, without buffering the whole result in memory before returning the result.
      // This allows to write data out to the client via a chunked HTTP response.
      mapRedSnapshot = snapshotInputProcessor.processParameters(ComputeMode.AFFINITY_CALL,snapshotView);
      mapRedContribution = inputProcessor.processParameters(ComputeMode.AFFINITY_CALL, contributionView);
    } else {
      mapRedSnapshot = snapshotInputProcessor.processParameters(snapshotView);
      mapRedContribution = inputProcessor.processParameters(contributionView);
    }
    //RequestParameters requestParameters = inputProcessor.getProcessingData().getRequestParameters();
    //RequestParameters requestParameters = inputProcessor.getProcessingData().getRequestParameters();
    String[] time = inputProcessor.splitParamOnComma(
        inputProcessor.createEmptyArrayIfNull(servletRequest.getParameterValues("time")));
    if (time.length != 2) {
      throw new BadRequestException(
          ExceptionMessages.TIME_FORMAT_CONTRS_EXTRACTION_AND_FULL_HISTORY);
    }
    TagTranslator tt = DbConnData.tagTranslator;
    String[] keys = inputProcessor.getKeys();
    final Set<Integer> keysInt = ExecutionUtils.keysToKeysInt(keys, tt);
    final ExecutionUtils exeUtils = new ExecutionUtils(inputProcessor.getProcessingData());
    inputProcessor.processPropertiesParam();
    inputProcessor.processIsUnclippedParam();
    //InputProcessingUtils utils = inputProcessor.getUtils();
    final boolean includeTags = inputProcessor.includeTags();
    final boolean includeOSMMetadata = inputProcessor.includeOSMMetadata();
    final boolean includeContributionTypes = inputProcessor.includeContributionTypes();
    final boolean clipGeometries = inputProcessor.isClipGeometry();
    final boolean isContributionsLatestEndpoint = operation instanceof Latest;
    final boolean isContributionsEndpoint =
        isContributionsLatestEndpoint || (operation instanceof ContributionsExtraction);
    final Set<SimpleFeatureType> simpleFeatureTypes = inputProcessor.getProcessingData().getSimpleFeatureTypes();
    String startTimestamp = IsoDateTimeParser.parseIsoDateTime(inputProcessor.getTime()[0])
        .format(DateTimeFormatter.ISO_DATE_TIME);
    String endTimestamp = IsoDateTimeParser.parseIsoDateTime(inputProcessor.getTime()[1])
        .format(DateTimeFormatter.ISO_DATE_TIME);
    MapReducer<List<OSMContribution>> mapRedContributions = mapRedContribution.groupByEntity();
    MapReducer<List<OSMEntitySnapshot>> mapRedSnapshots = mapRedSnapshot.groupByEntity();
    Optional<FilterExpression> filter = inputProcessor.getProcessingData().getFilterExpression();
    if (filter.isPresent()) {
      mapRedSnapshots = mapRedSnapshots.filter(filter.get());
      mapRedContributions = mapRedContributions.filter(filter.get());
    }
    final boolean isContainingSimpleFeatureTypes = inputProcessor.getProcessingData().isContainingSimpleFeatureTypes();
    DataExtractionTransformer dataExtractionTransformer = new DataExtractionTransformer(
        startTimestamp, endTimestamp, filter.orElse(null), isContributionsEndpoint,
        isContributionsLatestEndpoint,
        clipGeometries, includeTags, includeOSMMetadata, includeContributionTypes, exeUtils,
        keysInt, elementsGeometry, simpleFeatureTypes,
        isContainingSimpleFeatureTypes);
    MapReducer<Feature> contributionPreResult = mapRedContributions
        .flatMap(dataExtractionTransformer::buildChangedFeatures)
        .filter(Objects::nonNull);
    Metadata metadata = null;
    if (inputProcessor.getProcessingData().isShowMetadata()) {
      metadata = new Metadata(null, operation.getDescription(),
          inputProcessor.getRequestUrlIfGetRequest());
    }
    ExtractionResponse osmData = new ExtractionResponse(attribution, Application.API_VERSION,
        metadata, "FeatureCollection", Collections.emptyList());
    MapReducer<Feature> snapshotPreResult = null;
    if (!isContributionsEndpoint) {
      // handles cases where valid_from = t_start, valid_to = t_end; i.e. non-modified data
      snapshotPreResult = mapRedSnapshots
          .filter(snapshots -> snapshots.size() == 2)
          .filter(snapshots -> snapshots.get(0).getGeometry() == snapshots.get(1).getGeometry()
              && snapshots.get(0).getEntity().getVersion() == snapshots.get(1).getEntity()
                  .getVersion())
          .map(snapshots -> snapshots.get(0))
          .flatMap(dataExtractionTransformer::buildUnchangedFeatures)
          .filter(Objects::nonNull);
    }
    try (
        Stream<Feature> snapshotStream =
            (snapshotPreResult != null) ? snapshotPreResult.stream() : Stream.empty();
        Stream<Feature> contributionStream = contributionPreResult.stream()) {
      exeUtils.streamResponse(servletResponse, osmData,
          Stream.concat(contributionStream, snapshotStream));
    }
  }
}
