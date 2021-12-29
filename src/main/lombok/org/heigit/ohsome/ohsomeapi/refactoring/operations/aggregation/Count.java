package org.heigit.ohsome.ohsomeapi.refactoring.operations.aggregation;

import java.io.Serializable;
import java.util.List;
import java.util.SortedMap;
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
import org.heigit.ohsome.oshdb.util.mappable.OSMEntitySnapshot;
import org.locationtech.jts.geom.Geometry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Count implements Operation, SnapshotView {

  @Autowired
  InputProcessor inputProcessor;
  @Autowired
  DefaultAggregationResponse defaultAggregationResponse;
  @Autowired
  SnapshotView snapshotView;
  @Autowired
  ResultUtility resultUtility;

  @Override
  public List compute() throws Exception {
    final SortedMap<OSHDBTimestamp, ? extends Number> result;
    MapReducer<OSMEntitySnapshot> mapRed = inputProcessor.processParameters(snapshotView);
    result = getCountResult(mapRed.aggregateByTimestamp());
    Geometry geom = inputProcessor.getGeometry();
    return resultUtility.fillElementsResult(result, geom);
  }

  public <T extends Serializable & Comparable<T>> SortedMap<T, ? extends Number> getCountResult(MapAggregator<T, OSMEntitySnapshot> mapAggregator) throws Exception {
    return mapAggregator.count();
  }

  public <K extends Comparable<K> & Serializable, V extends Number> SortedMap<OSHDBCombinedIndex<OSHDBTimestamp, K>, V> getCountGroupByResult(MapAggregator<OSHDBCombinedIndex<OSHDBTimestamp, K>, OSMEntitySnapshot> mapAggregator) throws Exception {
    return (SortedMap<OSHDBCombinedIndex<OSHDBTimestamp, K>, V>) mapAggregator.map(OSMEntitySnapshot::getGeometry).count();
  }

  public <K extends Comparable<K> & Serializable, V extends Number>  SortedMap<OSHDBCombinedIndex<OSHDBCombinedIndex<Integer, K>, OSHDBTimestamp>, V> getCountGroupByBoundaryByTagResult(MapAggregator<OSHDBCombinedIndex<OSHDBCombinedIndex<Integer, K>, OSHDBTimestamp>, OSMEntitySnapshot> mapAggregator) throws Exception {
    return (SortedMap<OSHDBCombinedIndex<OSHDBCombinedIndex<Integer, K>, OSHDBTimestamp>, V>)
        mapAggregator.map(OSMEntitySnapshot::getGeometry).count();
  }

  @Override
  public Response getResponse(List resultSet) {
    return defaultAggregationResponse.getResponse(this, resultSet);
  }

  @Override
  public String getDescription() {
    return "count";
  }

  @Override
  public String getUnit() {
    return "absolute values";
  }
}
