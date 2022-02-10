package org.heigit.ohsome.ohsomeapi.refactoring.operations.aggregation;

import java.io.Serializable;
import java.util.List;
import java.util.SortedMap;
import org.heigit.ohsome.ohsomeapi.executor.ExecutionUtils;
import org.heigit.ohsome.ohsomeapi.inputprocessing.InputProcessor;
import org.heigit.ohsome.ohsomeapi.output.DefaultAggregationResponse;
import org.heigit.ohsome.ohsomeapi.output.Description;
import org.heigit.ohsome.ohsomeapi.output.Response;
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
import org.springframework.stereotype.Component;

@Component
public class Length implements Operation {

  @Autowired
  private InputProcessor inputProcessor;
  @Autowired
  ResultUtility resultUtility;
  @Autowired
  ExecutionUtils executionUtils;

  @Override
  public List compute() throws Exception {
    final SortedMap<OSHDBTimestamp, ? extends Number> result;
    MapReducer<OSMEntitySnapshot> mapRed = inputProcessor.getMapReducer();
    var mapRedGeom = mapRed.map(OSMEntitySnapshot::getGeometry);
    result = getLengthResult(mapRedGeom.aggregateByTimestamp());
    Geometry geom = inputProcessor.getGeometry();
    return resultUtility.fillElementsResult(result, geom, inputProcessor);
  }

  public SortedMap getLengthResult(MapAggregator mapAggregator) throws Exception {
    return mapAggregator.sum(geom -> executionUtils.cacheInUserData((Geometry) geom, () -> Geo.lengthOf(
        (Geometry) geom)));
  }

  public <K extends Comparable<K> & Serializable, V extends Number> SortedMap<OSHDBCombinedIndex<OSHDBTimestamp, K>, V> getLengthGroupByResult(MapAggregator<OSHDBCombinedIndex<OSHDBTimestamp, K>, OSMEntitySnapshot> mapAggregator) throws Exception {
    return (SortedMap<OSHDBCombinedIndex<OSHDBTimestamp, K>, V>) mapAggregator.map(OSMEntitySnapshot::getGeometry)
        .sum(geom -> executionUtils.cacheInUserData(geom, () -> Geo.lengthOf(geom)));
  }

  public <K extends Comparable<K> & Serializable, V extends Number> SortedMap<OSHDBCombinedIndex<OSHDBCombinedIndex<Integer, K>, OSHDBTimestamp>, V> getLengthGroupByBoundaryGroupByTagResult(MapAggregator<OSHDBCombinedIndex<OSHDBCombinedIndex<Integer, K>, OSHDBTimestamp>,
      OSMEntitySnapshot> mapAggregator) throws Exception {
    return (SortedMap<OSHDBCombinedIndex<OSHDBCombinedIndex<Integer, K>, OSHDBTimestamp>, V>)
        mapAggregator.map(OSMEntitySnapshot::getGeometry).sum(geom -> executionUtils.cacheInUserData(geom, () -> Geo.lengthOf(geom)));
  }

  @Override
  public Response getResponse(List resultSet) {
    return new DefaultAggregationResponse(resultSet, this);
  }

  public String getDescription() {
    return "length";
  }

  public String getUnit() {
    return "meters";
  }

  @Override
  public String getMetadataDescription() {
    return Description.aggregate(inputProcessor.isDensity(),
        this.getDescription(), this.getUnit());
  }

  @Override
  public InputProcessor getInputProcessor() {
    return inputProcessor;
  }
}
