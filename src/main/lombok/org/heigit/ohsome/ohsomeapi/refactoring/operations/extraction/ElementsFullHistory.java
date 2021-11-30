package org.heigit.ohsome.ohsomeapi.refactoring.operations.extraction;

import org.heigit.ohsome.ohsomeapi.output.DefaultAggregationResponse;
import org.heigit.ohsome.ohsomeapi.refactoring.operations.Operation;
import org.springframework.stereotype.Component;

@Component
public class ElementsFullHistory implements Operation {
  @Override
  public DefaultAggregationResponse compute() throws Exception {
    return null;
  }

  @Override
  public String getDescription() {
    return "Full-history OSM data as GeoJSON features.";
  }

  @Override
  public String getUnit() {
    return "";
  }
}
