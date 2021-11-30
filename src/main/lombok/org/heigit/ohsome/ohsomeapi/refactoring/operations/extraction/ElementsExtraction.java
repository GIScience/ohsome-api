package org.heigit.ohsome.ohsomeapi.refactoring.operations.extraction;

import org.heigit.ohsome.ohsomeapi.output.DefaultAggregationResponse;
import org.heigit.ohsome.ohsomeapi.refactoring.operations.Operation;
import org.springframework.stereotype.Component;

@Component
public class ElementsExtraction implements Operation {
  @Override
  public DefaultAggregationResponse compute() throws Exception {
    return null;
  }

  @Override
  public String getDescription() {
    return "OSM data as GeoJSON features.";
  }

  @Override
  public String getUnit() {
    return "";
  }
}
