package org.heigit.ohsome.ohsomeapi.executor;

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
import org.heigit.ohsome.ohsomeapi.inputprocessing.ProcessingData;
import org.heigit.ohsome.ohsomeapi.inputprocessing.SimpleFeatureType;
import org.heigit.ohsome.ohsomeapi.oshdb.DbConnData;
import org.heigit.ohsome.ohsomeapi.output.ExtractionResponse;
import org.heigit.ohsome.ohsomeapi.output.Metadata;
import org.heigit.ohsome.oshdb.api.db.OSHDBIgnite;
import org.heigit.ohsome.oshdb.api.db.OSHDBIgnite.ComputeMode;
import org.heigit.ohsome.oshdb.api.mapreducer.MapReducer;
import org.heigit.ohsome.oshdb.filter.FilterExpression;
import org.heigit.ohsome.oshdb.util.mappable.OSHDBMapReducible;
import org.heigit.ohsome.oshdb.util.mappable.OSMContribution;
import org.heigit.ohsome.oshdb.util.mappable.OSMEntitySnapshot;
import org.heigit.ohsome.oshdb.util.tagtranslator.TagTranslator;
import org.heigit.ohsome.oshdb.util.time.IsoDateTimeParser;
import org.wololo.geojson.Feature;

/** Holds executor methods for the following endpoints: /elementsFullHistory, /contributions. */
public class DataRequestExecutor extends RequestExecutor {

  private final RequestResource requestResource;
  private final InputProcessor inputProcessor;
  private final ProcessingData processingData;
  private final ElementsGeometry elementsGeometry;

  public DataRequestExecutor(RequestResource requestResource, ElementsGeometry elementsGeometry,
      HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
    super(servletRequest, servletResponse);
    this.requestResource = requestResource;
    this.elementsGeometry = elementsGeometry;
    inputProcessor = new InputProcessor(servletRequest, false, false);
    processingData = inputProcessor.getProcessingData();
  }

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
  public void extract() throws Exception {
    inputProcessor.getProcessingData().setFullHistory(true);
    final MapReducer<List<OSMContribution>> mapRedContributions = getMapReducer(inputProcessor);
    RequestParameters requestParameters = processingData.getRequestParameters();
    String[] time = inputProcessor.splitParamOnComma(
        inputProcessor.createEmptyArrayIfNull(servletRequest.getParameterValues("time")));
    if (time.length != 2) {
      throw new BadRequestException(
          ExceptionMessages.TIME_FORMAT_CONTRS_EXTRACTION_AND_FULL_HISTORY);
    }
    TagTranslator tt = DbConnData.tagTranslator;
    String[] keys = requestParameters.getKeys();
    final Set<Integer> keysInt = ExecutionUtils.keysToKeysInt(keys, tt);
    final ExecutionUtils exeUtils = new ExecutionUtils(processingData);
    inputProcessor.processPropertiesParam();
    inputProcessor.processIsUnclippedParam();
    InputProcessingUtils utils = inputProcessor.getUtils();
    final boolean includeTags = inputProcessor.includeTags();
    final boolean includeOSMMetadata = inputProcessor.includeOSMMetadata();
    final boolean includeContributionTypes = inputProcessor.includeContributionTypes();
    final boolean clipGeometries = inputProcessor.isClipGeometry();
    final boolean isContributionsLatestEndpoint =
        requestResource.equals(RequestResource.CONTRIBUTIONSLATEST);
    final boolean isContributionsEndpoint =
        isContributionsLatestEndpoint || requestResource.equals(RequestResource.CONTRIBUTIONS);
    final Set<SimpleFeatureType> simpleFeatureTypes = processingData.getSimpleFeatureTypes();
    String startTimestamp = IsoDateTimeParser.parseIsoDateTime(requestParameters.getTime()[0])
        .format(DateTimeFormatter.ISO_DATE_TIME);
    String endTimestamp = IsoDateTimeParser.parseIsoDateTime(requestParameters.getTime()[1])
        .format(DateTimeFormatter.ISO_DATE_TIME);
    final boolean isContainingSimpleFeatureTypes = processingData.isContainingSimpleFeatureTypes();
    DataExtractionTransformer dataExtractionTransformer = new DataExtractionTransformer(
        startTimestamp, endTimestamp, processingData.getFilterExpression().orElse(null),
        isContributionsEndpoint, isContributionsLatestEndpoint,
        clipGeometries, includeTags, includeOSMMetadata, includeContributionTypes, utils, exeUtils,
        keysInt, elementsGeometry, simpleFeatureTypes,
        isContainingSimpleFeatureTypes);
    MapReducer<Feature> contributionPreResult = mapRedContributions
        .flatMap(dataExtractionTransformer::buildChangedFeatures)
        .filter(Objects::nonNull);
    Metadata metadata = null;
    if (processingData.isShowMetadata()) {
      metadata = new Metadata(null, requestResource.getDescription(),
          inputProcessor.getRequestUrlIfGetRequest(servletRequest));
    }
    ExtractionResponse osmData = new ExtractionResponse(ATTRIBUTION, Application.API_VERSION,
        metadata, "FeatureCollection", Collections.emptyList());
    MapReducer<Feature> snapshotPreResult = null;
    if (!isContributionsEndpoint) {
      InputProcessor snapshotInputProcessor = new InputProcessor(servletRequest, true, false);
      snapshotInputProcessor.getProcessingData().setFullHistory(true);
      MapReducer<List<OSMEntitySnapshot>> mapRedSnapshots = getMapReducer(snapshotInputProcessor);
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

  private <X extends OSHDBMapReducible> MapReducer<List<X>> getMapReducer(
      InputProcessor inputProcessor) throws Exception {
    MapReducer<X> mapRed;
    if (DbConnData.db instanceof OSHDBIgnite) {
      // on ignite: Use AffinityCall backend, which is the only one properly supporting streaming
      // of result data, without buffering the whole result in memory before returning the result.
      // This allows to write data out to the client via a chunked HTTP response.
      mapRed = inputProcessor.processParameters(ComputeMode.AFFINITY_CALL);
    } else {
      mapRed = inputProcessor.processParameters();
    }
    MapReducer<List<X>> mapRedGrouped = mapRed.groupByEntity();
    Optional<FilterExpression> filter = processingData.getFilterExpression();
    return filter.map(mapRedGrouped::filter)
        .orElse(mapRedGrouped);
  }
}
