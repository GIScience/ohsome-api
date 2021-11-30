package org.heigit.ohsome.ohsomeapi.refactoring.operations;

import org.heigit.ohsome.ohsomeapi.output.DefaultAggregationResponse;
import org.springframework.stereotype.Component;

@Component
public interface Operation {

  DefaultAggregationResponse compute() throws Exception;

  String getDescription();

  String getUnit();

}
