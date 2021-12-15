package org.heigit.ohsome.ohsomeapi.executor;

import com.opencsv.CSVWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.function.Consumer;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.heigit.ohsome.ohsomeapi.Application;
import org.heigit.ohsome.ohsomeapi.inputprocessing.InputProcessingUtils;
import org.heigit.ohsome.ohsomeapi.inputprocessing.InputProcessor;
import org.heigit.ohsome.ohsomeapi.oshdb.ExtractMetadata;
import org.heigit.ohsome.ohsomeapi.output.Attribution;
import org.heigit.ohsome.ohsomeapi.output.DefaultAggregationResponse;
import org.heigit.ohsome.ohsomeapi.output.Description;
import org.heigit.ohsome.ohsomeapi.output.Metadata;
import org.heigit.ohsome.ohsomeapi.output.Response;
import org.heigit.ohsome.ohsomeapi.output.contributions.ContributionsResult;
import org.heigit.ohsome.ohsomeapi.output.elements.ElementsResult;
import org.heigit.ohsome.ohsomeapi.output.groupby.GroupByObject;
import org.heigit.ohsome.ohsomeapi.output.groupby.GroupByResponse;
import org.heigit.ohsome.ohsomeapi.output.groupby.GroupByResult;
import org.heigit.ohsome.ohsomeapi.output.ratio.RatioResult;
import org.heigit.ohsome.ohsomeapi.refactoring.operations.Operation;
import org.heigit.ohsome.ohsomeapi.refactoring.operations.SnapshotView;
import org.heigit.ohsome.ohsomeapi.refactoring.operations.aggregation.Area;
import org.heigit.ohsome.ohsomeapi.refactoring.operations.aggregation.Count;
import org.heigit.ohsome.ohsomeapi.refactoring.operations.aggregation.Length;
import org.heigit.ohsome.ohsomeapi.refactoring.operations.aggregation.Perimeter;
import org.heigit.ohsome.ohsomeapi.utilities.ResultUtility;
import org.heigit.ohsome.ohsomeapi.utils.RequestUtils;
import org.heigit.ohsome.oshdb.OSHDBTimestamp;
import org.heigit.ohsome.oshdb.api.mapreducer.MapReducer;
import org.heigit.ohsome.oshdb.util.geometry.Geo;
import org.heigit.ohsome.oshdb.util.mappable.OSMEntitySnapshot;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Polygonal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Holds relevant execution methods for various aggregation requests.
 */
@Component
public class AggregateRequestExecutor {

  @Autowired
  InputProcessor inputProcessor;
  @Autowired
  HttpServletRequest servletRequest;
  @Autowired
  HttpServletResponse servletResponse;
  @Autowired
  ResultUtility resultUtility;
  @Autowired
  ExtractMetadata extractMetadata;
  @Autowired
  InputProcessingUtils utils;
  @Autowired
  SnapshotView snapshotView;
  //private final InputProcessor inputProcessor;
  //private final ProcessingData processingData;
  private final long startTime = System.currentTimeMillis();
  boolean isDensity = false;
//  protected final String URL = extractMetadata.getAttributionUrl();
//  protected final String TEXT = extractMetadata.getAttributionShort();
  @Autowired
  Attribution attribution;
  @Autowired
  DefaultAggregationResponse defaultAggregationResponse;
  @Autowired
  GroupByResponse groupByResponse;
  public static final DecimalFormat df = ExecutionUtils.defineDecimalFormat("#.##");


//  public AggregateRequestExecutor(RequestResource requestResource,
//      HttpServletRequest servletRequest, HttpServletResponse servletResponse, boolean isDensity) {
//    super(servletRequest, servletResponse);
//    this.requestResource = requestResource;
//  this.isDensity = isDensity;
//    //inputProcessor = new InputProcessor(servletRequest, true, isDensity);
//    //processingData = inputProcessor.getProcessingData();
//  }

  public void setInputProcessor() {
    inputProcessor.create();
    inputProcessor.setSnapshot(true);
    inputProcessor.setDensity(isDensity);
  }

  /**
   * Performs a count|length|perimeter|area calculation.
   *
   * @return {@link org.heigit.ohsome.ohsomeapi.output.Response Response}
   * @throws RuntimeException if an unsupported RequestResource type is used. Only COUNT, LENGTH,
   *         PERIMETER, and AREA are permitted here
   * @throws Exception thrown by {@link org.heigit.ohsome.ohsomeapi.inputprocessing.InputProcessor
   *         #processParameters() processParameters},
   *         {@link org.heigit.ohsome.oshdb.api.mapreducer.MapAggregator#count() count}, or
   *         {@link org.heigit.ohsome.oshdb.api.mapreducer.MapAggregator#sum() sum}
   */
  public Response aggregate(Operation operation) throws Exception {
    final SortedMap<OSHDBTimestamp, ? extends Number> result;
    MapReducer<OSMEntitySnapshot> mapRed = null;
    //setInputProcessor();
    mapRed = inputProcessor.processParameters(snapshotView);
    var mapRedGeom = mapRed.map(OSMEntitySnapshot::getGeometry);
    if (operation instanceof Count) {
      result = mapRed.aggregateByTimestamp().count();
    } else if (operation instanceof Perimeter) {
      result = mapRedGeom.aggregateByTimestamp().sum(geom -> {
            if (!(geom instanceof Polygonal)) {
              return 0.0;
            }
            return ExecutionUtils.cacheInUserData(geom, () -> Geo.lengthOf(geom.getBoundary()));
          });
    } else if (operation instanceof Length) {
      result = mapRedGeom.aggregateByTimestamp()
          .sum(geom -> ExecutionUtils.cacheInUserData(geom, () -> Geo.lengthOf(geom)));
    } else if (operation instanceof Area) {
      result = mapRedGeom.aggregateByTimestamp()
          .sum(geom -> ExecutionUtils.cacheInUserData(geom, () -> Geo.areaOf(geom)));
    } else {
        throw new RuntimeException("Unsupported RequestResource type for this processing. "
            + "Only COUNT, LENGTH, PERIMETER, and AREA are permitted here");
    }
    Geometry geom = inputProcessor.getGeometry();
    //RequestParameters requestParameters = inputProcessor.getProcessingData().getRequestParameters();
    List resultSet = resultUtility.fillElementsResult(result, inputProcessor.isDensity(), geom);
    String description = Description.aggregate(inputProcessor.isDensity(),
        operation.getDescription(), operation.getUnit());
    Metadata metadata = generateMetadata(description);
//    if ("csv".equalsIgnoreCase(servletRequest.getParameter("format"))) {
//      return writeCsv(createCsvTopComments(metadata), writeCsvResponse(resultSet));
//    }
    //return defaultAggregationResponse.getJSONResponse(metadata, resultSet);


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
  public Response aggregateGroupByBoundary(Operation operation) throws Exception {
    inputProcessor.getProcessingData().setGroupByBoundary(true);
    MapReducer<OSMEntitySnapshot> mapRed = inputProcessor.processParameters(snapshotView);
    //RequestParameters requestParameters = inputProcessor.getProcessingData().getRequestParameters();
    //InputProcessingUtils utils = inputProcessor.getUtils();
    var result = computeCountLengthPerimeterAreaGbB(operation,
        inputProcessor.getProcessingData().getBoundaryType(), mapRed, inputProcessor);
    SortedMap<Integer, ? extends SortedMap<OSHDBTimestamp, ? extends Number>> groupByResult;
    groupByResult = ExecutionUtils.nest(result);
    GroupByResult[] resultSet = new GroupByResult[groupByResult.size()];
    Object groupByName;
    Object[] boundaryIds = utils.getBoundaryIds();
    int count = 0;
    ArrayList<Geometry> boundaries = new ArrayList<>(inputProcessor.getProcessingData().getBoundaryList());
    for (Entry<Integer, ? extends SortedMap<OSHDBTimestamp, ? extends Number>> entry : groupByResult
        .entrySet()) {
      List results = resultUtility.fillElementsResult(entry.getValue(), inputProcessor.isDensity(), boundaries.get(count));
      groupByName = boundaryIds[count];
      resultSet[count] = new GroupByResult(groupByName, results);
      count++;
    }
    String description = Description.aggregate(inputProcessor.isDensity(),
        operation.getDescription(), operation.getUnit());
    Metadata metadata = generateMetadata(description);
//    if ("geojson".equalsIgnoreCase(servletRequest.getParameter("format"))) {
//      return GroupByResponse.of(metadata, "FeatureCollection",
//          createGeoJsonFeatures(resultSet, inputProcessor.getProcessingData().getGeoJsonGeoms()));
//    } else if ("csv".equalsIgnoreCase(servletRequest.getParameter("format"))) {
//      return writeCsv(createCsvTopComments(metadata), writeCsvResponse(resultSet));
//    }
    //return new GroupByResponse(metadata, resultSet);
    return groupByResponse;
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
      for (ContributionsResult contributionsResult : rs) {
        writer.writeNext(new String[] {
            contributionsResult.getFromTimestamp(),
            contributionsResult.getToTimestamp(),
            String.valueOf(contributionsResult.getValue())
        });
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
    if (!RequestUtils.cacheNotAllowed(inputProcessor.getProcessingData().getRequestUrl(),
        inputProcessor.getTime())) {
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
            CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END)) {
      writer.writeAll(comments, false);
      consumer.accept(writer);
    }
    // no response needed as writer has already been called
    return null;
  }

  /** Creates the comments of the csv response (Attribution, API-Version and optional Metadata). */
  private List<String[]> createCsvTopComments(Metadata metadata) {
    List<String[]> comments = new LinkedList<>();
    comments.add(new String[] {"# Copyright URL: " + extractMetadata.getAttributionUrl()});
    comments.add(new String[] {"# Copyright Text: " + extractMetadata.getAttributionShort()});
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
      for (int j = 0; j < groupByResult.getResult().size(); j++) {
        ElementsResult elemResult = (ElementsResult) groupByResult.getResult().get(j);
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
}
