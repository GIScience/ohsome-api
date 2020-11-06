package org.heigit.ohsome.ohsomeapi.executor;

import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Stream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.heigit.bigspatialdata.oshdb.api.db.OSHDBIgnite;
import org.heigit.bigspatialdata.oshdb.api.db.OSHDBIgnite.ComputeMode;
import org.heigit.bigspatialdata.oshdb.api.mapreducer.MapReducer;
import org.heigit.bigspatialdata.oshdb.api.object.OSMContribution;
import org.heigit.bigspatialdata.oshdb.api.object.OSMEntitySnapshot;
import org.heigit.bigspatialdata.oshdb.osm.OSMEntity;
import org.heigit.bigspatialdata.oshdb.util.celliterator.ContributionType;
import org.heigit.bigspatialdata.oshdb.util.tagtranslator.TagTranslator;
import org.heigit.bigspatialdata.oshdb.util.time.ISODateTimeParser;
import org.heigit.bigspatialdata.oshdb.util.time.TimestampFormatter;
import org.heigit.ohsome.filter.FilterExpression;
import org.heigit.ohsome.ohsomeapi.Application;
import org.heigit.ohsome.ohsomeapi.controller.rawdata.ElementsGeometry;
import org.heigit.ohsome.ohsomeapi.exception.BadRequestException;
import org.heigit.ohsome.ohsomeapi.exception.ExceptionMessages;
import org.heigit.ohsome.ohsomeapi.inputprocessing.InputProcessingUtils;
import org.heigit.ohsome.ohsomeapi.inputprocessing.InputProcessor;
import org.heigit.ohsome.ohsomeapi.inputprocessing.ProcessingData;
import org.heigit.ohsome.ohsomeapi.inputprocessing.SimpleFeatureType;
import org.heigit.ohsome.ohsomeapi.oshdb.DbConnData;
import org.heigit.ohsome.ohsomeapi.output.dataaggregationresponse.Metadata;
import org.heigit.ohsome.ohsomeapi.output.rawdataresponse.DataResponse;
import org.locationtech.jts.geom.Geometry;
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
   * @throws Exception thrown by
   *         {@link org.heigit.ohsome.ohsomeapi.inputprocessing.InputProcessor#processParameters()
   *         processParameters},
   *         {@link org.heigit.bigspatialdata.oshdb.util.time.ISODateTimeParser#parseISODateTime(String)
   *         parseISODateTime},
   *         {@link org.heigit.bigspatialdata.oshdb.api.mapreducer.MapReducer#stream() stream}, or
   *         {@link org.heigit.ohsome.ohsomeapi.executor.ExecutionUtils#streamResponse(HttpServletResponse, DataResponse, Stream)
   *         streamElementsResponse}
   */
  public void extract() throws Exception {
    inputProcessor.getProcessingData().setFullHistory(true);
    InputProcessor snapshotInputProcessor = new InputProcessor(servletRequest, true, false);
    snapshotInputProcessor.getProcessingData().setFullHistory(true);
    MapReducer<OSMEntitySnapshot> mapRedSnapshot = null;
    MapReducer<OSMContribution> mapRedContribution = null;
    if (DbConnData.db instanceof OSHDBIgnite) {
      // on ignite: Use AffinityCall backend, which is the only one properly supporting streaming
      // of result data, without buffering the whole result in memory before returning the result.
      // This allows to write data out to the client via a chunked HTTP response.
      mapRedSnapshot = snapshotInputProcessor.processParameters(ComputeMode.AffinityCall);
      mapRedContribution = inputProcessor.processParameters(ComputeMode.AffinityCall);
    } else {
      mapRedSnapshot = snapshotInputProcessor.processParameters();
      mapRedContribution = inputProcessor.processParameters();
    }
    RequestParameters requestParameters = processingData.getRequestParameters();
    String[] time = inputProcessor.splitParamOnComma(
        inputProcessor.createEmptyArrayIfNull(servletRequest.getParameterValues("time")));
    if (time.length != 2) {
      throw new BadRequestException(ExceptionMessages.TIME_FORMAT_FULL_HISTORY);
    }
    TagTranslator tt = DbConnData.tagTranslator;
    String[] keys = requestParameters.getKeys();
    int[] keysInt = new int[keys.length];
    if (keys.length != 0) {
      for (int i = 0; i < keys.length; i++) {
        keysInt[i] = tt.getOSHDBTagKeyOf(keys[i]).toInt();
      }
    }
    ExecutionUtils exeUtils = new ExecutionUtils(processingData);
    inputProcessor.processPropertiesParam();
    inputProcessor.processIsUnclippedParam();
    InputProcessingUtils utils = inputProcessor.getUtils();
    final boolean includeTags = inputProcessor.includeTags();
    final boolean includeOSMMetadata = inputProcessor.includeOSMMetadata();
    final boolean clipGeometries = inputProcessor.isClipGeometry();
    final boolean isContributionsLatestEndpoint =
        requestResource.equals(RequestResource.CONTRIBUTIONSLATEST);
    final boolean isContributionsEndpoint =
        (isContributionsLatestEndpoint || requestResource.equals(RequestResource.CONTRIBUTIONS))
            ? true
            : false;
    final Set<SimpleFeatureType> simpleFeatureTypes = processingData.getSimpleFeatureTypes();
    Optional<FilterExpression> filter = processingData.getFilterExpression();
    final boolean requiresGeometryTypeCheck =
        filter.isPresent() && ProcessingData.filterContainsGeometryTypeCheck(filter.get());
    FilterExpression filterExpression = processingData.getFilterExpression().orElse(null);
    String startTimestamp = ISODateTimeParser.parseISODateTime(requestParameters.getTime()[0])
        .format(DateTimeFormatter.ISO_DATE_TIME);
    String endTimestamp = ISODateTimeParser.parseISODateTime(requestParameters.getTime()[1])
        .format(DateTimeFormatter.ISO_DATE_TIME);
    MapReducer<List<OSMContribution>> mapRedContributions = mapRedContribution.groupByEntity();
    MapReducer<Feature> contributionPreResult = mapRedContributions.flatMap(contributions -> {
      List<Feature> output = new LinkedList<>();
      Map<String, Object> properties;
      Geometry currentGeom = null;
      OSMEntity currentEntity = null;
      String validFrom = null;
      String validTo;
      boolean skipNext = false;
      if (!isContributionsLatestEndpoint) {
        // first contribution:
        OSMContribution firstContribution = contributions.get(0);
        if (firstContribution.is(ContributionType.CREATION) && !isContributionsEndpoint) {
          skipNext = true;
        } else {
          // if not "creation": take "before" as starting "row" (geom, tags), valid_from = t_start
          currentEntity = firstContribution.getEntityBefore();
          currentGeom = exeUtils.getGeometry(firstContribution, clipGeometries, true);
          validFrom = startTimestamp;
        }
        // then for each contribution:
        for (int i = 0; i < contributions.size(); i++) {
          if (i == contributions.size() - 1 && isContributionsEndpoint) {
            // end the loop when last contribution is reached as it gets added later on
            break;
          }
          OSMContribution contribution = contributions.get(i);
          if (isContributionsEndpoint) {
            currentEntity = contribution.getEntityAfter();
            currentGeom = exeUtils.getGeometry(contribution, clipGeometries, false);
            validFrom = TimestampFormatter.getInstance().isoDateTime(contribution.getTimestamp());
          }
          // set valid_to of previous row
          validTo = TimestampFormatter.getInstance().isoDateTime(contribution.getTimestamp());
          if (!skipNext && currentGeom != null && !currentGeom.isEmpty()) {
            boolean addToOutput = addEntityToOutput(processingData, utils, simpleFeatureTypes,
                requiresGeometryTypeCheck, filterExpression, currentGeom, currentEntity);
            if (addToOutput) {
              properties = new TreeMap<>();
              if (!isContributionsEndpoint) {
                properties.put("@validFrom", validFrom);
                properties.put("@validTo", validTo);
              } else {
                properties.put("@timestamp", validTo);
              }
              output.add(exeUtils.createOSMFeature(currentEntity, currentGeom, properties, keysInt,
                  includeTags, includeOSMMetadata, isContributionsEndpoint, elementsGeometry,
                  contribution.getContributionTypes()));
            }
          }
          skipNext = false;
          if (contribution.is(ContributionType.DELETION)) {
            // if deletion: skip output of next row
            skipNext = true;
          } else if (!isContributionsEndpoint) {
            // else: take "after" as next row
            currentEntity = contribution.getEntityAfter();
            currentGeom = exeUtils.getGeometry(contribution, clipGeometries, false);
            validFrom = TimestampFormatter.getInstance().isoDateTime(contribution.getTimestamp());
          }
        }
      }
      // after loop:
      OSMContribution lastContribution = contributions.get(contributions.size() - 1);
      currentGeom = exeUtils.getGeometry(lastContribution, clipGeometries, false);
      currentEntity = lastContribution.getEntityAfter();
      if (!lastContribution.is(ContributionType.DELETION)) {
        // if last contribution was not "deletion": set valid_to = t_end
        validTo = endTimestamp;
        properties = new TreeMap<>();
        if (!isContributionsEndpoint) {
          properties.put("@validFrom", validFrom);
          properties.put("@validTo", validTo);
        } else {
          properties.put("@timestamp",
              TimestampFormatter.getInstance().isoDateTime(lastContribution.getTimestamp()));
        }
        if (!currentGeom.isEmpty()) {
          boolean addToOutput = addEntityToOutput(processingData, utils, simpleFeatureTypes,
              requiresGeometryTypeCheck, filterExpression, currentGeom, currentEntity);
          if (addToOutput) {
            output.add(exeUtils.createOSMFeature(currentEntity, currentGeom, properties, keysInt,
                includeTags, includeOSMMetadata, isContributionsEndpoint, elementsGeometry,
                lastContribution.getContributionTypes()));
          }
        }
      } else if (isContributionsEndpoint) {
        // adds the deletion feature for a /contributions request
        currentGeom = exeUtils.getGeometry(lastContribution, clipGeometries, true);
        properties = new TreeMap<>();
        properties.put("@timestamp",
            TimestampFormatter.getInstance().isoDateTime(lastContribution.getTimestamp()));
        output.add(exeUtils.createOSMFeature(currentEntity, currentGeom, properties, keysInt, false,
            includeOSMMetadata, isContributionsEndpoint, elementsGeometry,
            lastContribution.getContributionTypes()));
      }
      return output;
    }).filter(Objects::nonNull);
    Metadata metadata = null;
    if (processingData.isShowMetadata()) {
      metadata = new Metadata(null, requestResource.getDescription(),
          inputProcessor.getRequestUrlIfGetRequest(servletRequest));
    }
    DataResponse osmData = new DataResponse(ATTRIBUTION, Application.API_VERSION, metadata,
        "FeatureCollection", Collections.emptyList());
    MapReducer<Feature> snapshotPreResult = null;
    if (!isContributionsEndpoint) {
      // handles cases where valid_from = t_start, valid_to = t_end; i.e. non-modified data
      snapshotPreResult = mapRedSnapshot.groupByEntity().filter(snapshots -> snapshots.size() == 2)
          .filter(snapshots -> snapshots.get(0).getGeometry() == snapshots.get(1).getGeometry()
              && snapshots.get(0).getEntity().getVersion() == snapshots.get(1).getEntity()
                  .getVersion())
          .map(snapshots -> snapshots.get(0)).flatMap(snapshot -> {
            Map<String, Object> properties = new TreeMap<>();
            OSMEntity entity = snapshot.getEntity();
            if (includeOSMMetadata) {
              properties.put("@lastEdit", entity.getTimestamp().toString());
            }
            Geometry geom = snapshot.getGeometry();
            if (!clipGeometries) {
              geom = snapshot.getGeometryUnclipped();
            }
            properties.put("@snapshotTimestamp",
                TimestampFormatter.getInstance().isoDateTime(snapshot.getTimestamp()));
            properties.put("@validFrom", startTimestamp);
            properties.put("@validTo", endTimestamp);
            boolean addToOutput = addEntityToOutput(processingData, utils, simpleFeatureTypes,
                requiresGeometryTypeCheck, filterExpression, geom, entity);
            if (addToOutput) {
              return Collections.singletonList(
                  exeUtils.createOSMFeature(entity, geom, properties, keysInt, includeTags,
                      includeOSMMetadata, isContributionsEndpoint, elementsGeometry, null));
            } else {
              return Collections.emptyList();
            }
          }).filter(Objects::nonNull);
    }
    try (
        Stream<Feature> snapshotStream =
            (snapshotPreResult != null) ? snapshotPreResult.stream() : Stream.empty();
        Stream<Feature> contributionStream = contributionPreResult.stream()) {
      exeUtils.streamResponse(servletResponse, osmData,
          Stream.concat(contributionStream, snapshotStream));
    }
  }

  /** Checks whether the given entity should be added to the output (true) or not (false). */
  private boolean addEntityToOutput(ProcessingData processingData, InputProcessingUtils utils,
      final Set<SimpleFeatureType> simpleFeatureTypes, final boolean requiresGeometryTypeCheck,
      FilterExpression filterExpression, Geometry currentGeom, OSMEntity currentEntity) {
    boolean addToOutput;
    if (processingData.isContainingSimpleFeatureTypes()) {
      addToOutput = utils.checkGeometryOnSimpleFeatures(currentGeom, simpleFeatureTypes);
    } else if (requiresGeometryTypeCheck) {
      addToOutput = filterExpression.applyOSMGeometry(currentEntity, currentGeom);
    } else {
      addToOutput = true;
    }
    return addToOutput;
  }

}
