package org.heigit.ohsome.ohsomeapi.refactoring.operations.aggregation;

import java.util.List;
import java.util.SortedMap;
import org.heigit.ohsome.ohsomeapi.inputprocessing.InputProcessor;
import org.heigit.ohsome.ohsomeapi.output.DefaultAggregationResponse;
import org.heigit.ohsome.ohsomeapi.output.Response;
import org.heigit.ohsome.ohsomeapi.refactoring.operations.Operation;
import org.heigit.ohsome.ohsomeapi.refactoring.operations.SnapshotView;
import org.heigit.ohsome.ohsomeapi.utilities.ResultUtility;
import org.heigit.ohsome.oshdb.OSHDBTimestamp;
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
      result = mapRed.aggregateByTimestamp().count();
    Geometry geom = inputProcessor.getGeometry();
    List resultSet = resultUtility.fillElementsResult(result, inputProcessor.isDensity(), geom);
  return resultSet;
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
