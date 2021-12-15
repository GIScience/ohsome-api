package org.heigit.ohsome.ohsomeapi.refactoring.operations.aggregation;

import java.util.List;
import org.heigit.ohsome.ohsomeapi.output.DefaultAggregationResponse;
import org.heigit.ohsome.ohsomeapi.output.Response;
import org.heigit.ohsome.ohsomeapi.refactoring.operations.Operation;
import org.heigit.ohsome.ohsomeapi.refactoring.operations.SnapshotView;
import org.springframework.stereotype.Component;

@Component
public class Ratio implements Operation, SnapshotView {

  @Override
  public DefaultAggregationResponse compute() throws Exception {
    return null;
  }

  @Override
  public Response getResponse(List resultSet) {
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
