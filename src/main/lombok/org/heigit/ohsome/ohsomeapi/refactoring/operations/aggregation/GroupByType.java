package org.heigit.ohsome.ohsomeapi.refactoring.operations.aggregation;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import javax.servlet.http.HttpServletRequest;
import org.heigit.ohsome.ohsomeapi.inputprocessing.InputProcessor;
import org.heigit.ohsome.ohsomeapi.output.Description;
import org.heigit.ohsome.ohsomeapi.output.Response;
import org.heigit.ohsome.ohsomeapi.output.Result;
import org.heigit.ohsome.ohsomeapi.output.groupby.GroupByResponse;
import org.heigit.ohsome.ohsomeapi.output.groupby.GroupByResult;
import org.heigit.ohsome.ohsomeapi.refactoring.operations.Operation;
import org.heigit.ohsome.ohsomeapi.utilities.ResultUtility;
import org.heigit.ohsome.oshdb.OSHDBTimestamp;
import org.heigit.ohsome.oshdb.api.generic.OSHDBCombinedIndex;
import org.heigit.ohsome.oshdb.api.mapreducer.MapAggregator;
import org.heigit.ohsome.oshdb.api.mapreducer.MapReducer;
import org.heigit.ohsome.oshdb.osm.OSMType;
import org.heigit.ohsome.oshdb.util.function.SerializableFunction;
import org.heigit.ohsome.oshdb.util.mappable.OSMEntitySnapshot;
import org.locationtech.jts.geom.Geometry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GroupByType extends Group implements Operation {

  @Autowired
  private ResultUtility resultUtility;
  @Autowired
  private InputProcessor inputProcessor;

  /**
   * Performs a count|length|perimeter|area calculation grouped by the OSM type.
   *
   * @param requestResource {@link org.heigit.ohsome.ohsomeapi.executor.RequestResource
   *     RequestResource} definition of the request resource
   * @param servletRequest {@link HttpServletRequest HttpServletRequest} incoming
   *     request object
   * @param servletResponse {@link javax.servlet.http.HttpServletResponse HttpServletResponse]}
   *     outgoing response object
   * @param isSnapshot whether this request uses the snapshot-view (true), or contribution-view
   *     (false)
   * @param isDensity whether this request is accessed via the /density resource
   * @return {@link org.heigit.ohsome.ohsomeapi.output.Response Response}
   * @throws Exception thrown by {@link org.heigit.ohsome.ohsomeapi.inputprocessing.InputProcessor
   *     #getMapReducer() getMapReducer} and
   *     {@link org.heigit.ohsome.ohsomeapi.executor.ExecutionUtils
   *     #computeResult(RequestResource, MapAggregator) computeResult}
   */
  public MapAggregator<OSHDBCombinedIndex<OSHDBTimestamp, OSMType>, OSMEntitySnapshot> compute() throws Exception {
    return aggregate(inputProcessor.getMapReducer());
  }

  public <T> List<GroupByResult> getResult(SortedMap<OSHDBCombinedIndex<OSHDBTimestamp, OSMType>, Number> preResult) {

    var groupByResult = nest(preResult);
    List<GroupByResult> resultSet = new ArrayList<>();
    Geometry geom = inputProcessor.getGeometry();
    for (var entry : groupByResult.entrySet()) {
      List<Result> results = resultUtility.fillElementsResult(entry.getValue(), geom, inputProcessor);
      resultSet.add(new GroupByResult(entry.getKey().toString(), results));
    }
    return resultSet;
  }

  public MapReducer<OSMEntitySnapshot> getMapReducer() throws Exception {
    return inputProcessor.getMapReducer();
  }

  private MapAggregator<OSHDBCombinedIndex<OSHDBTimestamp, OSMType>, OSMEntitySnapshot> aggregate(
      MapReducer<OSMEntitySnapshot> mapRed) {
    return mapRed.aggregateByTimestamp().aggregateBy(
        (SerializableFunction<OSMEntitySnapshot, OSMType>) f -> f.getEntity().getType(),
        inputProcessor.getProcessingData().getOsmTypes());
  }

  @Override
  public Response getResponse(List resultSet) {
    return new GroupByResponse(resultSet, this);
  }

  @Override
  public String getDescription() {
    return "";
  }

  @Override
  public String getUnit() {
    return "";
  }

  @Override
  public String getMetadataDescription(){
    return Description.countPerimeterAreaGroupByType(inputProcessor.isDensity(),
            this.getDescription(), this.getUnit());
  }

  @Override
  public InputProcessor getInputProcessor() {
    return inputProcessor;
  }
}
