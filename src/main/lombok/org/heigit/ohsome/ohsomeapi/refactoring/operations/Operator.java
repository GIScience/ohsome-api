package org.heigit.ohsome.ohsomeapi.refactoring.operations;

import java.util.ArrayList;
import java.util.List;
import org.heigit.ohsome.ohsomeapi.output.DefaultAggregationResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@Component
@RequestScope
public class Operator implements Operation{
  private final List<Operation> operationList = new ArrayList<>();

  @Override
  public DefaultAggregationResponse compute() throws Exception {
    DefaultAggregationResponse response = null;
    for (Operation op : operationList) {
     return op.compute();
    }
    return response;
    //return op;
  }

  public void setOperation(Operation operation) {
    operationList.add(operation);
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
