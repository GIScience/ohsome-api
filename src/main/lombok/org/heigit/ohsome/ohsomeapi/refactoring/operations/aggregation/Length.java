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
public class Length implements Operation, SnapshotView {

  @Autowired
  InputProcessor inputProcessor;
  @Autowired
  SnapshotView snapshotView;
  @Autowired
  ResultUtility resultUtility;
  @Autowired
  DefaultAggregationResponse defaultAggregationResponse;

  @Override
  public List compute() throws Exception {
    final SortedMap<OSHDBTimestamp, ? extends Number> result;
    MapReducer<OSMEntitySnapshot> mapRed = inputProcessor.processParameters(snapshotView);
    var mapRedGeom = mapRed.map(OSMEntitySnapshot::getGeometry);
    result = getLength(mapRedGeom.aggregateByTimestamp());
    Geometry geom = inputProcessor.getGeometry();
    List resultSet = resultUtility.fillElementsResult(result, inputProcessor.isDensity(), geom);
    return resultSet;
  }

  public SortedMap getLength(MapAggregator mapAggregator) throws Exception {
    return mapAggregator.sum(geom -> ExecutionUtils.cacheInUserData((Geometry) geom, () -> Geo.lengthOf(
        (Geometry) geom)));
  }

  public <K extends Comparable<K> & Serializable, V extends Number> SortedMap<OSHDBCombinedIndex<OSHDBTimestamp, K>, V> getLengthGroupBy(MapAggregator<OSHDBCombinedIndex<OSHDBTimestamp, K>, OSMEntitySnapshot> mapAggregator) throws Exception {
    return (SortedMap<OSHDBCombinedIndex<OSHDBTimestamp, K>, V>) mapAggregator.map(OSMEntitySnapshot::getGeometry)
        .sum(geom -> ExecutionUtils.cacheInUserData(geom, () -> Geo.lengthOf(geom)));
  }

  public <K extends Comparable<K> & Serializable, V extends Number> SortedMap<OSHDBCombinedIndex<OSHDBCombinedIndex<Integer, K>, OSHDBTimestamp>, V> getLengthGroupByBoundaryGroupByTag(MapAggregator<OSHDBCombinedIndex<OSHDBCombinedIndex<Integer, K>, OSHDBTimestamp>,
      OSMEntitySnapshot> mapAggregator) throws Exception {
    return (SortedMap<OSHDBCombinedIndex<OSHDBCombinedIndex<Integer, K>, OSHDBTimestamp>, V>)
        mapAggregator.map(OSMEntitySnapshot::getGeometry).sum(geom -> ExecutionUtils.cacheInUserData(geom, () -> Geo.lengthOf(geom)));
  }

  @Override
  public Response getResponse(List resultSet) {
    return defaultAggregationResponse.getResponse(this, resultSet);
  }

  public String getDescription() {
    return "length";
  }

  public String getUnit() {
    return "meters";
  }
}
