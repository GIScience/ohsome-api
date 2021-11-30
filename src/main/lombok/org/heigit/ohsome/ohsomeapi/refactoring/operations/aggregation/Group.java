package org.heigit.ohsome.ohsomeapi.refactoring.operations.aggregation;

import org.heigit.ohsome.ohsomeapi.output.DefaultAggregationResponse;
import org.heigit.ohsome.ohsomeapi.refactoring.operations.Operation;
import org.springframework.stereotype.Component;

@Component
public class Group implements Operation {
  @Override
  public DefaultAggregationResponse compute() throws Exception {
    return null;
  }

  @Override
  public String getDescription() {
    return "";
  }

  @Override
  public String getUnit() {
    return "";
  }
}
