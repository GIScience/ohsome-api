package org.heigit.ohsome.ohsomeapi.refactoring;

import java.util.SortedMap;
import lombok.Getter;
import lombok.Setter;
import org.heigit.ohsome.oshdb.api.mapreducer.MapAggregator;
import org.heigit.ohsome.oshdb.api.mapreducer.MapReducer;

@Getter
@Setter
public class Mapper {

  private MapReducer red;
  private MapAggregator aggr;
  private SortedMap sortedMap;
}
