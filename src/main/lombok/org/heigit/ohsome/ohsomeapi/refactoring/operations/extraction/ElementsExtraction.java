package org.heigit.ohsome.ohsomeapi.refactoring.operations.extraction;

import org.heigit.ohsome.ohsomeapi.output.Response;
import org.heigit.ohsome.ohsomeapi.output.Result;
import org.heigit.ohsome.ohsomeapi.refactoring.operations.Operation;
import org.springframework.stereotype.Component;

@Component
public class ElementsExtraction implements Operation {

  @Override
  public Object compute() throws Exception {
    return null;
  }

  @Override
  public Response getResponse(Result[] resultSet) {
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
