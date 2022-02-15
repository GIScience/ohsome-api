package org.heigit.ohsome.ohsomeapi.refactoring.operations.aggregation;

import java.io.Serializable;
import java.util.List;
import java.util.SortedMap;
import lombok.Getter;
import org.heigit.ohsome.ohsomeapi.executor.ExecutionUtils;
import org.heigit.ohsome.ohsomeapi.inputprocessing.InputProcessor;
import org.heigit.ohsome.ohsomeapi.output.DefaultAggregationResponse;
import org.heigit.ohsome.ohsomeapi.output.Description;
import org.heigit.ohsome.ohsomeapi.output.Response;
import org.heigit.ohsome.ohsomeapi.output.Result;
import org.heigit.ohsome.ohsomeapi.refactoring.operations.Operation;
import org.heigit.ohsome.ohsomeapi.utilities.ResultUtility;
import org.heigit.ohsome.oshdb.OSHDBTimestamp;
import org.heigit.ohsome.oshdb.api.generic.OSHDBCombinedIndex;
import org.heigit.ohsome.oshdb.api.mapreducer.MapAggregator;
import org.heigit.ohsome.oshdb.api.mapreducer.MapReducer;
import org.heigit.ohsome.oshdb.util.geometry.Geo;
import org.heigit.ohsome.oshdb.util.mappable.OSMEntitySnapshot;
import org.locationtech.jts.geom.Geometry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.RequestScope;

@Service
@RequestScope
public class Area implements Operation {

  @Getter
  private final InputProcessor inputProcessor;
  private final ResultUtility resultUtility;
  private final ExecutionUtils executionUtils;

  @Autowired
  public Area(InputProcessor inputProcessor, ResultUtility resultUtility,
      ExecutionUtils executionUtils) {
    this.inputProcessor = inputProcessor;
    this.resultUtility = resultUtility;
    this.executionUtils = executionUtils;
  }

  public List<Result> compute() throws Exception {
    MapReducer<OSMEntitySnapshot> mapRed = inputProcessor.getMapReducer();
    var mapRedGeom = mapRed.map(OSMEntitySnapshot::getGeometry);
    var result = getAreaResult(mapRedGeom.aggregateByTimestamp());
    Geometry geom = inputProcessor.getGeometry();
    List<Result> objects = resultUtility.fillElementsResult(result, geom, inputProcessor);
    return objects;
  }

  public <U extends Comparable<U>, R extends Number> SortedMap<U, R> getAreaResult(
      MapAggregator<? extends Comparable, ? extends Comparable> mapAggregator) throws Exception {
    return (SortedMap<U, R>) mapAggregator.sum(geom -> executionUtils.cacheInUserData((Geometry) geom,
        () -> Geo.areaOf((Geometry) geom)));
  }

  public <K extends Comparable<K> & Serializable, V extends Number> SortedMap<OSHDBCombinedIndex<OSHDBTimestamp, K>, V> getAreaGroupByResult(MapAggregator<OSHDBCombinedIndex<OSHDBTimestamp, K>, OSMEntitySnapshot> mapAggregator) throws Exception {
    return (SortedMap<OSHDBCombinedIndex<OSHDBTimestamp, K>, V>) mapAggregator.map(OSMEntitySnapshot::getGeometry)
        .sum(geom -> executionUtils.cacheInUserData(geom, () -> Geo.areaOf(geom)));
  }

  public <K extends Comparable<K> & Serializable, V extends Number> SortedMap<OSHDBCombinedIndex<OSHDBCombinedIndex<Integer, K>, OSHDBTimestamp>, V> getAreaGroupByBoundaryByTagResult(MapAggregator<OSHDBCombinedIndex<OSHDBCombinedIndex<Integer, K>, OSHDBTimestamp>, OSMEntitySnapshot> mapAggregator) throws Exception {
    return (SortedMap<OSHDBCombinedIndex<OSHDBCombinedIndex<Integer, K>, OSHDBTimestamp>, V>)
        mapAggregator.map(OSMEntitySnapshot::getGeometry)
            .sum(geom -> executionUtils.cacheInUserData(geom, () -> Geo.areaOf(geom)));
  }

  @Override
  public Response getResponse(List resultSet) {
    return new DefaultAggregationResponse(resultSet, this);
  }

  @Override
  public String getDescription() {
    return "area";
  }

  @Override
  public String getUnit() {
    return "square meters";
  }

  @Override
  public String getMetadataDescription() {
    return Description.aggregate(inputProcessor.isDensity(),
      this.getDescription(), this.getUnit());
  }
}
