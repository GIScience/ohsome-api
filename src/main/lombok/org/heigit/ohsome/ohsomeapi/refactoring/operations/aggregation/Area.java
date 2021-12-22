package org.heigit.ohsome.ohsomeapi.refactoring.operations.aggregation;

import java.io.Serializable;
import java.util.List;
import java.util.SortedMap;
import org.heigit.ohsome.ohsomeapi.executor.ExecutionUtils;
import org.heigit.ohsome.ohsomeapi.inputprocessing.InputProcessor;
import org.heigit.ohsome.ohsomeapi.output.DefaultAggregationResponse;
import org.heigit.ohsome.ohsomeapi.output.Response;
import org.heigit.ohsome.ohsomeapi.refactoring.operations.Operation;
import org.heigit.ohsome.ohsomeapi.refactoring.operations.SnapshotView;
import org.heigit.ohsome.ohsomeapi.utilities.ResultUtility;
import org.heigit.ohsome.oshdb.OSHDBTimestamp;
import org.heigit.ohsome.oshdb.api.generic.OSHDBCombinedIndex;
import org.heigit.ohsome.oshdb.api.mapreducer.MapAggregator;
import org.heigit.ohsome.oshdb.api.mapreducer.MapReducer;
import org.heigit.ohsome.oshdb.util.geometry.Geo;
import org.heigit.ohsome.oshdb.util.mappable.OSMEntitySnapshot;
import org.locationtech.jts.geom.Geometry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Area implements Operation, SnapshotView {

  @Autowired
  InputProcessor inputProcessor;
  @Autowired
  ResultUtility resultUtility;
  @Autowired
  SnapshotView snapshotView;
  @Autowired
  DefaultAggregationResponse defaultAggregationResponse;


  @Override
  public List compute() throws Exception {
    final SortedMap<OSHDBTimestamp, ? extends Number> result;
    MapReducer<OSMEntitySnapshot> mapRed = inputProcessor.processParameters(snapshotView);
    var mapRedGeom = mapRed.map(OSMEntitySnapshot::getGeometry);
    //      result = mapRedGeom.aggregateByTimestamp()
    //          .sum(geom -> ExecutionUtils.cacheInUserData(geom, () -> Geo.areaOf(geom)));
    result = getArea(mapRedGeom.aggregateByTimestamp());
    Geometry geom = inputProcessor.getGeometry();
    List resultSet = resultUtility.fillElementsResult(result, inputProcessor.isDensity(), geom);
    return resultSet;
  }

  public SortedMap getArea(MapAggregator mapAggr) throws Exception {
    return mapAggr.sum(geom -> ExecutionUtils.cacheInUserData((Geometry) geom, () -> Geo.areaOf(
        (Geometry) geom)));
  }

  public <K extends Comparable<K> & Serializable, V extends Number> SortedMap<OSHDBCombinedIndex<OSHDBTimestamp, K>, V> getAreaGroupBy(MapAggregator<OSHDBCombinedIndex<OSHDBTimestamp, K>, OSMEntitySnapshot> mapAggregator) throws Exception {
    return (SortedMap<OSHDBCombinedIndex<OSHDBTimestamp, K>, V>) mapAggregator.map(OSMEntitySnapshot::getGeometry)
        .sum(geom -> ExecutionUtils.cacheInUserData(geom, () -> Geo.areaOf(geom)));
  }

  public <K extends Comparable<K> & Serializable, V extends Number>  SortedMap<OSHDBCombinedIndex<OSHDBCombinedIndex<Integer, K>, OSHDBTimestamp>, V> getAreaGroupByBoundaryByTag(MapAggregator<OSHDBCombinedIndex<OSHDBCombinedIndex<Integer, K>, OSHDBTimestamp>, OSMEntitySnapshot> mapAggregator) throws Exception {
    return (SortedMap<OSHDBCombinedIndex<OSHDBCombinedIndex<Integer, K>, OSHDBTimestamp>, V>)
        mapAggregator.map(OSMEntitySnapshot::getGeometry).sum(geom -> ExecutionUtils.cacheInUserData(geom, () -> Geo.areaOf(geom)));
  }

  @Override
  public Response getResponse(List resultSet) {
    return defaultAggregationResponse.getResponse(this, resultSet);
  }

    @Override
    public String getDescription() {
      return "area";
    }

    @Override
    public String getUnit() {
      return "square meters";
    }
  }
