package org.heigit.ohsome.ohsomeapi.refactoring.operations.aggregation;

import org.heigit.ohsome.ohsomeapi.output.DefaultAggregationResponse;
import org.heigit.ohsome.ohsomeapi.refactoring.operations.Operation;
import org.springframework.stereotype.Component;

@Component
public class GroupByBoundary implements Operation, GroupBy {
  @Override
  public void group() {

  }

  @Override
  public DefaultAggregationResponse compute() throws Exception {
    return null;
  }

  @Override
  public String getDescription() {
    return null;
  }

  @Override
  public String getUnit() {
    return null;
  }
}
