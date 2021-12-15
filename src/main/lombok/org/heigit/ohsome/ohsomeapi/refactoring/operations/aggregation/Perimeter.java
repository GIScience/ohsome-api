package org.heigit.ohsome.ohsomeapi.refactoring.operations.aggregation;

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
    result = mapRedGeom.aggregateByTimestamp().sum(geom -> {
      if (!(geom instanceof Polygonal)) {
        return 0.0;
      }
      return ExecutionUtils.cacheInUserData(geom, () -> Geo.lengthOf(geom.getBoundary()));
    });
    Geometry geom = inputProcessor.getGeometry();
    List resultSet = resultUtility.fillElementsResult(result, inputProcessor.isDensity(), geom);
    return resultSet;
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
