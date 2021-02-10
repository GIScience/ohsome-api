package org.heigit.ohsome.ohsomeapi.executor;

import static org.heigit.ohsome.ohsomeapi.utils.GroupByBoundaryGeoJsonGenerator.createGeoJsonFeatures;
import com.opencsv.CSVWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.SortedMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.heigit.bigspatialdata.oshdb.api.generic.OSHDBCombinedIndex;
import org.heigit.bigspatialdata.oshdb.api.generic.function.SerializableFunction;
import org.heigit.bigspatialdata.oshdb.api.mapreducer.MapAggregator;
import org.heigit.bigspatialdata.oshdb.api.mapreducer.MapReducer;
import org.heigit.bigspatialdata.oshdb.api.object.OSMEntitySnapshot;
import org.heigit.bigspatialdata.oshdb.util.OSHDBTimestamp;
import org.heigit.bigspatialdata.oshdb.util.geometry.Geo;
import org.heigit.bigspatialdata.oshdb.util.time.TimestampFormatter;
import org.heigit.ohsome.filter.FilterExpression;
import org.heigit.ohsome.ohsomeapi.Application;
import org.heigit.ohsome.ohsomeapi.exception.BadRequestException;
import org.heigit.ohsome.ohsomeapi.exception.ExceptionMessages;
import org.heigit.ohsome.ohsomeapi.inputprocessing.BoundaryType;
import org.heigit.ohsome.ohsomeapi.inputprocessing.InputProcessingUtils;
import org.heigit.ohsome.ohsomeapi.inputprocessing.InputProcessor;
import org.heigit.ohsome.ohsomeapi.inputprocessing.ProcessingData;
import org.heigit.ohsome.ohsomeapi.output.DefaultAggregationResponse;
import org.heigit.ohsome.ohsomeapi.output.Description;
import org.heigit.ohsome.ohsomeapi.output.Metadata;
import org.heigit.ohsome.ohsomeapi.output.Response;
import org.heigit.ohsome.ohsomeapi.output.dataaggregationresponse.users.ContributionsResult;
import org.heigit.ohsome.ohsomeapi.output.elements.ElementsResult;
import org.heigit.ohsome.ohsomeapi.output.groupby.GroupByObject;
import org.heigit.ohsome.ohsomeapi.output.groupby.GroupByResponse;
import org.heigit.ohsome.ohsomeapi.output.groupby.GroupByResult;
import org.heigit.ohsome.ohsomeapi.output.ratio.RatioResult;
import org.heigit.ohsome.ohsomeapi.utils.RequestUtils;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Polygonal;

/**
 * Holds the relevant execution methods for aggregation requests under /elements and
 * /elements/_/groupBy/boundary.
 */
public class AggregateRequestExecutor extends RequestExecutor {

  private final RequestResource requestResource;
  private final InputProcessor inputProcessor;
  private final ProcessingData processingData;
  private final long startTime = System.currentTimeMillis();

  public AggregateRequestExecutor(RequestResource requestResource,
      HttpServletRequest servletRequest, HttpServletResponse servletResponse, boolean isDensity) {
    super(servletRequest, servletResponse);
    this.requestResource = requestResource;
    inputProcessor = new InputProcessor(servletRequest, true, isDensity);
    processingData = inputProcessor.getProcessingData();
  }

  /**
   * Performs a count|length|perimeter|area calculation.
   * 
   * @return {@link org.heigit.ohsome.ohsomeapi.output.Response Response}
   * @throws RuntimeException if an unsupported RequestResource type is used. Only COUNT, LENGTH,
   *         PERIMETER, and AREA are permitted here
   * @throws Exception thrown by {@link org.heigit.ohsome.ohsomeapi.inputprocessing.InputProcessor
   *         #processParameters() processParameters},
   *         {@link org.heigit.bigspatialdata.oshdb.api.mapreducer.MapAggregator#count() count}, or
   *         {@link org.heigit.bigspatialdata.oshdb.api.mapreducer.MapAggregator#sum() sum}
   */
  public Response aggregate() throws Exception {
    final SortedMap<OSHDBTimestamp, ? extends Number> result;
    MapReducer<OSMEntitySnapshot> mapRed = null;
    mapRed = inputProcessor.processParameters();
    switch (requestResource) {
      case COUNT:
        result = mapRed.aggregateByTimestamp().count();
        break;
      case AREA:
        result = mapRed.aggregateByTimestamp()
            .sum((SerializableFunction<OSMEntitySnapshot, Number>) snapshot -> ExecutionUtils
                .cacheInUserData(snapshot.getGeometry(), () -> Geo.areaOf(snapshot.getGeometry())));
        break;
      case LENGTH:
        result = mapRed.aggregateByTimestamp()
            .sum((SerializableFunction<OSMEntitySnapshot, Number>) snapshot -> ExecutionUtils
                .cacheInUserData(snapshot.getGeometry(),
                    () -> Geo.lengthOf(snapshot.getGeometry())));
        break;
      case PERIMETER:
        result = mapRed.aggregateByTimestamp()
            .sum((SerializableFunction<OSMEntitySnapshot, Number>) snapshot -> {
              if (snapshot.getGeometry() instanceof Polygonal) {
                return ExecutionUtils.cacheInUserData(snapshot.getGeometry(),
                    () -> Geo.lengthOf(snapshot.getGeometry().getBoundary()));
              } else {
                return 0.0;
              }
            });
        break;
      default:
        throw new RuntimeException("Unsupported RequestResource type for this processing. "
            + "Only COUNT, LENGTH, PERIMETER, and AREA are permitted here");
    }
    Geometry geom = inputProcessor.getGeometry();
    RequestParameters requestParameters = processingData.getRequestParameters();
    ElementsResult[] resultSet =
        fillElementsResult(result, requestParameters.isDensity(), df, geom);
    String description = Description.aggregate(requestParameters.isDensity(),
        requestResource.getDescription(), requestResource.getUnit());
    Metadata metadata = generateMetadata(description);
    if ("csv".equalsIgnoreCase(requestParameters.getFormat())) {
      return writeCsv(createCsvTopComments(metadata), writeCsvResponse(resultSet));
    }
    return DefaultAggregationResponse.of(ATTRIBUTION, Application.API_VERSION, metadata, resultSet);
  }

  /**
   * Performs a count|length|perimeter|area calculation grouped by the boundary.
   * 
   * @return {@link org.heigit.ohsome.ohsomeapi.output.Response Response}
   * @throws Exception thrown by {@link org.heigit.ohsome.ohsomeapi.inputprocessing.InputProcessor
   *         #processParameters() processParameters} and
   *         {@link org.heigit.ohsome.ohsomeapi.executor.AggregateRequestExecutor
   *         #computeCountLengthPerimeterAreaGbB(RequestResource, BoundaryType, MapReducer,
   *         InputProcessor) computeCountLengthPerimeterAreaGbB}
   */
  public Response aggregateGroupByBoundary() throws Exception {
    processingData.setGroupByBoundary(true);
    RequestParameters requestParameters = processingData.getRequestParameters();
    MapReducer<OSMEntitySnapshot> mapRed = inputProcessor.processParameters();
    InputProcessingUtils utils = inputProcessor.getUtils();
    var result = computeCountLengthPerimeterAreaGbB(requestResource,
        processingData.getBoundaryType(), mapRed, inputProcessor);
    SortedMap<Integer, ? extends SortedMap<OSHDBTimestamp, ? extends Number>> groupByResult;
    groupByResult = ExecutionUtils.nest(result);
    GroupByResult[] resultSet = new GroupByResult[groupByResult.size()];
    Object groupByName;
    Object[] boundaryIds = utils.getBoundaryIds();
    int count = 0;
    ArrayList<Geometry> boundaries = new ArrayList<>(processingData.getBoundaryList());
    for (Entry<Integer, ? extends SortedMap<OSHDBTimestamp, ? extends Number>> entry : groupByResult
        .entrySet()) {
      ElementsResult[] results = fillElementsResult(entry.getValue(), requestParameters.isDensity(),
          df, boundaries.get(count));
      groupByName = boundaryIds[count];
      resultSet[count] = new GroupByResult(groupByName, results);
      count++;
    }
    String description = Description.aggregate(requestParameters.isDensity(),
        requestResource.getDescription(), requestResource.getUnit());
    Metadata metadata = generateMetadata(description);
    if ("geojson".equalsIgnoreCase(requestParameters.getFormat())) {
      return GroupByResponse.of(ATTRIBUTION, Application.API_VERSION, metadata, "FeatureCollection",
          createGeoJsonFeatures(resultSet, processingData.getGeoJsonGeoms()));
    } else if ("csv".equalsIgnoreCase(requestParameters.getFormat())) {
      return writeCsv(createCsvTopComments(metadata), writeCsvResponse(resultSet));
    }
    return new GroupByResponse(ATTRIBUTION, Application.API_VERSION, metadata, resultSet);
  }

  /**
   * Creates the metadata for the JSON response containing info like execution time, request URL and
   * a short description of the returned data.
   */
  private Metadata generateMetadata(String description) {
    Metadata metadata = null;
    if (processingData.isShowMetadata()) {
      long duration = System.currentTimeMillis() - startTime;
      metadata = new Metadata(duration, description,
          inputProcessor.getRequestUrlIfGetRequest(servletRequest));
    }
    return metadata;
  }

  /**
   * Writes a response in the csv format for /count|length|perimeter|area(/density)(/ratio)|groupBy
   * requests.
   */
  private Consumer<CSVWriter> writeCsvResponse(Object[] resultSet) {
    return writer -> writeCsvResponse(writer, resultSet);
  }

  /** Writing of the CSV response for different types of result sets. */
  private void writeCsvResponse(CSVWriter writer, Object[] resultSet) {
    if (resultSet instanceof ElementsResult[]) {
      ElementsResult[] rs = (ElementsResult[]) resultSet;
      writer.writeNext(new String[] {"timestamp", "value"}, false);
      for (ElementsResult elementsResult : rs) {
        writer.writeNext(new String[] {elementsResult.getTimestamp(),
            String.valueOf(elementsResult.getValue())});
      }
    } else if (resultSet instanceof ContributionsResult[]) {
      ContributionsResult[] rs = (ContributionsResult[]) resultSet;
      writer.writeNext(new String[] {"fromTimestamp", "toTimestamp", "value"}, false);
      for (ContributionsResult ContributionsResult : rs) {
        writer.writeNext(new String[] {ContributionsResult.getFromTimestamp(), ContributionsResult.getToTimestamp(),
            String.valueOf(ContributionsResult.getValue())});
      }
    } else if (resultSet instanceof RatioResult[]) {
      RatioResult[] rs = (RatioResult[]) resultSet;
      writer.writeNext(new String[] {"timestamp", "value", "value2", "ratio"}, false);
      for (RatioResult ratioResult : rs) {
        writer.writeNext(
            new String[] {ratioResult.getTimestamp(), String.valueOf(ratioResult.getValue()),
                String.valueOf(ratioResult.getValue2()), String.valueOf(ratioResult.getRatio())});
      }
    } else if (resultSet instanceof GroupByResult[]) {
      GroupByObject[] rs = (GroupByResult[]) resultSet;
      if (resultSet.length == 0) {
        writer.writeNext(new String[] {"timestamp"}, false);
      } else {
        var rows = createCsvResponseForElementsGroupBy(rs);
        writer.writeNext(rows.getLeft().toArray(new String[rows.getLeft().size()]), false);
        writer.writeAll(rows.getRight(), false);
      }
    }
  }

  /** Defines character encoding, content type and cache header in given servlet response object. */
  private void setCsvSettingsInServletResponse() {
    servletResponse.setCharacterEncoding("UTF-8");
    servletResponse.setContentType("text/csv");
    if (!RequestUtils.cacheNotAllowed(processingData.getRequestUrl(),
        processingData.getRequestParameters().getTime())) {
      servletResponse.setHeader("Cache-Control", "no-transform, public, max-age=31556926");
    }
  }

  /**
   * Writes the CSV response directly and returns a null Response as writer has already been called.
   * 
   * @throws IOException thrown by {@link javax.servlet.ServletResponse#getWriter() getWriter}
   */
  private Response writeCsv(List<String[]> comments, Consumer<CSVWriter> consumer)
      throws IOException {
    setCsvSettingsInServletResponse();
    try (CSVWriter writer =
        new CSVWriter(servletResponse.getWriter(), ';', CSVWriter.DEFAULT_QUOTE_CHARACTER,
            CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END);) {
      writer.writeAll(comments, false);
      consumer.accept(writer);
    }
    // no response needed as writer has already been called
    return null;
  }

  /** Creates the comments of the csv response (Attribution, API-Version and optional Metadata). */
  private List<String[]> createCsvTopComments(Metadata metadata) {
    List<String[]> comments = new LinkedList<>();
    comments.add(new String[] {"# Copyright URL: " + URL});
    comments.add(new String[] {"# Copyright Text: " + TEXT});
    comments.add(new String[] {"# API Version: " + Application.API_VERSION});
    if (metadata != null) {
      comments.add(new String[] {"# Execution Time: " + metadata.getExecutionTime()});
      comments.add(new String[] {"# Description: " + metadata.getDescription()});
      if (metadata.getRequestUrl() != null) {
        comments.add(new String[] {"# Request URL: " + metadata.getRequestUrl()});
      }
    }
    return comments;
  }

  /**
   * Creates the csv response for /elements/_/groupBy requests.
   * 
   * @param resultSet <code>GroupByObject</code> array containing <code>GroupByResult</code> objects
   *        containing <code>ElementsResult</code> objects
   * @return <code>Pair</code> containing the column names (left) and the data rows (right)
   */
  private ImmutablePair<List<String>, List<String[]>> createCsvResponseForElementsGroupBy(
      GroupByObject[] resultSet) {
    List<String> columnNames = new LinkedList<>();
    columnNames.add("timestamp");
    List<String[]> rows = new LinkedList<>();
    for (int i = 0; i < resultSet.length; i++) {
      GroupByResult groupByResult = (GroupByResult) resultSet[i];
      Object groupByObject = groupByResult.getGroupByObject();
      if (groupByObject instanceof Object[]) {
        Object[] groupByObjectArr = (Object[]) groupByObject;
        columnNames.add(groupByObjectArr[0].toString() + "_" + groupByObjectArr[1].toString());
      } else {
        columnNames.add(groupByObject.toString());
      }
      for (int j = 0; j < groupByResult.getResult().length; j++) {
        ElementsResult elemResult = (ElementsResult) groupByResult.getResult()[j];
        if (i == 0) {
          String[] row = new String[resultSet.length + 1];
          row[0] = elemResult.getTimestamp();
          row[1] = String.valueOf(elemResult.getValue());
          rows.add(row);
        } else {
          rows.get(j)[i + 1] = String.valueOf(elemResult.getValue());
        }
      }
    }
    return new ImmutablePair<>(columnNames, rows);
  }

  /** Fills the ElementsResult array with respective ElementsResult objects. */
  private ElementsResult[] fillElementsResult(SortedMap<OSHDBTimestamp, ? extends Number> entryVal,
      boolean isDensity, DecimalFormat df, Geometry geom) {
    ElementsResult[] results = new ElementsResult[entryVal.entrySet().size()];
    int count = 0;
    for (Entry<OSHDBTimestamp, ? extends Number> entry : entryVal.entrySet()) {
      if (isDensity) {
        results[count] = new ElementsResult(
            TimestampFormatter.getInstance().isoDateTime(entry.getKey()), Double.parseDouble(
                df.format(entry.getValue().doubleValue() / (Geo.areaOf(geom) * 0.000001))));
      } else {
        results[count] =
            new ElementsResult(TimestampFormatter.getInstance().isoDateTime(entry.getKey()),
                Double.parseDouble(df.format(entry.getValue().doubleValue())));
      }
      count++;
    }
    return results;
  }

  /**
   * Computes the result for the /count|length|perimeter|area/groupBy/boundary resources.
   * 
   * @throws BadRequestException if a boundary parameter is not defined.
   * @throws Exception thrown by {@link org.heigit.bigspatialdata.oshdb.api.mapreducer.MapAggregator
   *         #count() count}, or
   *         {@link org.heigit.bigspatialdata.oshdb.api.mapreducer.MapAggregator
   *         #sum(SerializableFunction) sum}
   */
  private <P extends Geometry & Polygonal> SortedMap<OSHDBCombinedIndex<OSHDBTimestamp, Integer>, 
        ? extends Number> computeCountLengthPerimeterAreaGbB(RequestResource requestResource, 
        BoundaryType boundaryType, MapReducer<OSMEntitySnapshot> mapRed, 
        InputProcessor inputProcessor) throws Exception {
    if (boundaryType == BoundaryType.NOBOUNDARY) {
      throw new BadRequestException(ExceptionMessages.NO_BOUNDARY);
    }
    MapAggregator<OSHDBCombinedIndex<OSHDBTimestamp, Integer>, Geometry> preResult;
    ArrayList<Geometry> arrGeoms = new ArrayList<>(processingData.getBoundaryList());
    @SuppressWarnings("unchecked") // intentionally as check for P on Polygonal is already performed
    Map<Integer, P> geoms = IntStream.range(0, arrGeoms.size()).boxed()
        .collect(Collectors.toMap(idx -> idx, idx -> (P) arrGeoms.get(idx)));
    MapAggregator<OSHDBCombinedIndex<OSHDBTimestamp, Integer>, OSMEntitySnapshot> mapAgg =
        mapRed.aggregateByTimestamp().aggregateByGeometry(geoms);
    if (processingData.isContainingSimpleFeatureTypes()) {
      mapAgg = inputProcessor.filterOnSimpleFeatures(mapAgg);
    }
    Optional<FilterExpression> filter = processingData.getFilterExpression();
    if (filter.isPresent()) {
      mapAgg = mapAgg.filter(filter.get());
    }
    preResult = mapAgg.map(OSMEntitySnapshot::getGeometry);
    switch (requestResource) {
      case COUNT:
        return preResult.count();
      case PERIMETER:
        return preResult.sum(geom -> {
          if (!(geom instanceof Polygonal)) {
            return 0.0;
          }
          return ExecutionUtils.cacheInUserData(geom, () -> Geo.lengthOf(geom.getBoundary()));
        });
      case LENGTH:
        return preResult
            .sum(geom -> ExecutionUtils.cacheInUserData(geom, () -> Geo.lengthOf(geom)));
      case AREA:
        return preResult.sum(geom -> ExecutionUtils.cacheInUserData(geom, () -> Geo.areaOf(geom)));
      default:
        return null;
    }
  }
}
