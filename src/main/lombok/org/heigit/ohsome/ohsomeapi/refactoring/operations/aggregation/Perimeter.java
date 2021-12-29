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
import org.locationtech.jts.geom.Polygonal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Perimeter implements Operation, SnapshotView {

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
    result = getPerimeterResult(mapRedGeom.aggregateByTimestamp());
    Geometry geom = inputProcessor.getGeometry();
    return resultUtility.fillElementsResult(result, geom);
  }

  public SortedMap getPerimeterResult(MapAggregator mapAggregator) throws Exception {
    return mapAggregator.sum(geom -> {
      if (!(geom instanceof Polygonal)) {
        return 0.0;
      }
      return ExecutionUtils.cacheInUserData((Geometry) geom, () -> Geo.lengthOf(((Geometry) geom).getBoundary()));
    });
  }

  public <K extends Comparable<K> & Serializable, V extends Number> SortedMap<OSHDBCombinedIndex<OSHDBTimestamp, K>, V> getPerimeterGroupByResult(MapAggregator<OSHDBCombinedIndex<OSHDBTimestamp, K>, OSMEntitySnapshot> mapAggregator) throws Exception {
    return (SortedMap<OSHDBCombinedIndex<OSHDBTimestamp, K>, V>) mapAggregator.map(OSMEntitySnapshot::getGeometry).sum(geom -> {
      if (!(geom instanceof Polygonal)) {
        return 0.0;
      }
      return ExecutionUtils.cacheInUserData(geom, () -> Geo.lengthOf(geom.getBoundary()));
    });
  }

  public  <K extends Comparable<K> & Serializable, V extends Number> SortedMap<OSHDBCombinedIndex<OSHDBCombinedIndex<Integer, K>, OSHDBTimestamp>, V> getPerimeterGroupByBoundaryGroupByTagResult(MapAggregator<OSHDBCombinedIndex<OSHDBCombinedIndex<Integer, K>, OSHDBTimestamp>,
      OSMEntitySnapshot> mapAggregator) throws Exception {
    return (SortedMap<OSHDBCombinedIndex<OSHDBCombinedIndex<Integer, K>, OSHDBTimestamp>, V>) mapAggregator.map(OSMEntitySnapshot::getGeometry).sum(geom -> {
      if (!(geom instanceof Polygonal)) {
        return 0.0;
      }
      return ExecutionUtils.cacheInUserData(geom, () -> Geo.lengthOf(geom.getBoundary()));
    });
  }

  @Override
  public Response getResponse(List resultSet) {
    return defaultAggregationResponse.getResponse(this, resultSet);
  }

  public String getDescription() {
    return "perimeter";
  }

  public String getUnit() {
    return "meters";
  }
}
