package org.heigit.ohsome.ohsomeapi.refactoring.operations;

import org.heigit.ohsome.ohsomeapi.output.DefaultAggregationResponse;
import org.springframework.stereotype.Component;

@Component
public class Latest implements Operation{
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
