package org.heigit.ohsome.ohsomeapi.refactoring.operations.aggregation;

import javax.servlet.http.HttpServletRequest;
import org.heigit.ohsome.ohsomeapi.Application;
import org.heigit.ohsome.ohsomeapi.executor.ExecutionUtils;
import org.heigit.ohsome.ohsomeapi.inputprocessing.InputProcessor;
import org.heigit.ohsome.ohsomeapi.inputprocessing.ProcessingData;
import org.heigit.ohsome.ohsomeapi.output.Attribution;
import org.heigit.ohsome.ohsomeapi.output.DefaultAggregationResponse;
import org.heigit.ohsome.ohsomeapi.output.Description;
import org.heigit.ohsome.ohsomeapi.output.Metadata;
import org.heigit.ohsome.ohsomeapi.output.elements.ElementsResult;
import org.heigit.ohsome.ohsomeapi.output.groupby.GroupByResponse;
import org.heigit.ohsome.ohsomeapi.output.groupby.GroupByResult;
import org.heigit.ohsome.ohsomeapi.refactoring.operations.Operation;
import org.heigit.ohsome.oshdb.OSHDBTimestamp;
import org.heigit.ohsome.oshdb.api.generic.OSHDBCombinedIndex;
import org.heigit.ohsome.oshdb.api.mapreducer.MapAggregator;
import org.heigit.ohsome.oshdb.api.mapreducer.MapReducer;
import org.heigit.ohsome.oshdb.osm.OSMType;
import org.heigit.ohsome.oshdb.util.function.SerializableFunction;
import org.heigit.ohsome.oshdb.util.mappable.OSMEntitySnapshot;
import org.jetbrains.annotations.NotNull;
import org.locationtech.jts.geom.Geometry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GroupByType implements Operation, GroupBy {

  @Autowired
  InputProcessor inputProcessor;
  @Autowired
  HttpServletRequest servletRequest;
  @Autowired
  Attribution attribution;

  @Override
  public DefaultAggregationResponse compute() throws Exception {
  group();
  }
    /**
     * Performs a count|length|perimeter|area calculation grouped by the OSM type.
     *
     * @param requestResource {@link org.heigit.ohsome.ohsomeapi.executor.RequestResource
     *        RequestResource} definition of the request resource
     * @param servletRequest {@link javax.servlet.http.HttpServletRequest HttpServletRequest} incoming
     *        request object
     * @param servletResponse {@link javax.servlet.http.HttpServletResponse HttpServletResponse]}
     *        outgoing response object
     * @param isSnapshot whether this request uses the snapshot-view (true), or contribution-view
     *        (false)
     * @param isDensity whether this request is accessed via the /density resource
     * @return {@link org.heigit.ohsome.ohsomeapi.output.Response Response}
     * @throws Exception thrown by {@link org.heigit.ohsome.ohsomeapi.inputprocessing.InputProcessor
     *         #processParameters() processParameters} and
     *         {@link org.heigit.ohsome.ohsomeapi.executor.ExecutionUtils
     *         #computeResult(RequestResource, MapAggregator) computeResult}
     */
    public MapAggregator<OSHDBCombinedIndex<OSHDBTimestamp, OSMType>, OSMEntitySnapshot> group(boolean isSnapshot, boolean isDensity) throws Exception {
      final long startTime = System.currentTimeMillis();
      MapReducer<OSMEntitySnapshot> mapRed = null;
      inputProcessor.setSnapshot(isSnapshot);
      inputProcessor.setDensity(isDensity);
      //InputProcessor inputProcessor = new InputProcessor(servletRequest, isSnapshot, isDensity);
      mapRed = inputProcessor.processParameters();
      ProcessingData processingData = inputProcessor.getProcessingData();
      //RequestParameters requestParameters = processingData.getRequestParameters();
      MapAggregator<OSHDBCombinedIndex<OSHDBTimestamp, OSMType>, OSMEntitySnapshot> preResult;
      preResult = mapRed.aggregateByTimestamp().aggregateBy(
          (SerializableFunction<OSMEntitySnapshot, OSMType>) f -> f.getEntity().getType(),
          processingData.getOsmTypes());
      return getGroupByResponse(startTime, processingData, preResult);
    }

  @NotNull
  private GroupByResponse getGroupByResponse(long startTime, ProcessingData processingData,
      MapAggregator<OSHDBCombinedIndex<OSHDBTimestamp, OSMType>, OSMEntitySnapshot> preResult) throws Exception {
    var result = ExecutionUtils.computeResult(operation, preResult);
    var groupByResult = ExecutionUtils.nest(result);
    GroupByResult[] resultSet = new GroupByResult[groupByResult.size()];
    Geometry geom = inputProcessor.getGeometry();
    int count = 0;
    for (var entry : groupByResult.entrySet()) {
      ElementsResult[] results = ExecutionUtils.fillElementsResult(
          entry.getValue(), inputProcessor.isDensity(), df, geom);
      resultSet[count] = new GroupByResult(entry.getKey().toString(), results);
      count++;
    }
    Metadata metadata = null;
    if (processingData.isShowMetadata()) {
      long duration = System.currentTimeMillis() - startTime;
      metadata = new Metadata(duration,
          Description.countPerimeterAreaGroupByType(inputProcessor.isDensity(),
              operation.getDescription(), operation.getUnit()),
          inputProcessor.getRequestUrlIfGetRequest());
    }
    //      if ("csv".equalsIgnoreCase(servletRequest.getParameter("format"))) {
    //        ExecutionUtils exeUtils = new ExecutionUtils(processingData);
    //        exeUtils.writeCsvResponse(resultSet, servletResponse,
    //            ExecutionUtils.createCsvTopComments(attribution.getUrl(), attribution.getText(), Application.API_VERSION, metadata));
    //        return null;
    //      }
    return new GroupByResponse(attribution, Application.API_VERSION, metadata,
        resultSet);
  }

  @Override
  public String getDescription() {
    return null;
  }

  @Override
  public String getUnit() {
    return null;
  }
}
