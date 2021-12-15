package org.heigit.ohsome.ohsomeapi.refactoring.operations.aggregation;

import org.heigit.ohsome.ohsomeapi.output.DefaultAggregationResponse;
import org.heigit.ohsome.ohsomeapi.refactoring.operations.Operation;
import org.heigit.ohsome.ohsomeapi.refactoring.operations.SnapshotView;
import org.springframework.stereotype.Component;

@Component
public class Density implements Operation, SnapshotView {

  private final String description = "density";
  private final String unit = "meters";

  @Override
  public DefaultAggregationResponse compute() throws Exception {
    return null;
  }

  public String getDescription() {
    return description;
  }

  public String getUnit() {
    return unit;
  }
}
