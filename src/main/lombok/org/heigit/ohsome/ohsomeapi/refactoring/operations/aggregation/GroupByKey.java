package org.heigit.ohsome.ohsomeapi.refactoring.operations.aggregation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedMap;
import lombok.Getter;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.heigit.ohsome.ohsomeapi.executor.ExecutionUtils;
import org.heigit.ohsome.ohsomeapi.inputprocessing.InputProcessor;
import org.heigit.ohsome.ohsomeapi.oshdb.DbConnData;
import org.heigit.ohsome.ohsomeapi.output.Description;
import org.heigit.ohsome.ohsomeapi.output.Response;
import org.heigit.ohsome.ohsomeapi.output.Result;
import org.heigit.ohsome.ohsomeapi.output.groupby.GroupByResponse;
import org.heigit.ohsome.ohsomeapi.output.groupby.GroupByResult;
import org.heigit.ohsome.ohsomeapi.refactoring.operations.Operation;
import org.heigit.ohsome.ohsomeapi.utilities.GroupByUtility;
import org.heigit.ohsome.ohsomeapi.utilities.ResultUtility;
import org.heigit.ohsome.oshdb.OSHDBTag;
import org.heigit.ohsome.oshdb.OSHDBTimestamp;
import org.heigit.ohsome.oshdb.api.generic.OSHDBCombinedIndex;
import org.heigit.ohsome.oshdb.api.mapreducer.MapAggregator;
import org.heigit.ohsome.oshdb.api.mapreducer.MapReducer;
import org.heigit.ohsome.oshdb.util.mappable.OSMEntitySnapshot;
import org.heigit.ohsome.oshdb.util.tagtranslator.TagTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.RequestScope;

@Service
@RequestScope
public class GroupByKey implements Operation<MapAggregator<OSHDBCombinedIndex<OSHDBTimestamp, Integer>, OSMEntitySnapshot>> {

  private final ResultUtility resultUtility;
  private Integer [] keysInt;
  @Getter
  private final InputProcessor inputProcessor;
  private final GroupByUtility groupByUtility;

  @Autowired
  public GroupByKey(GroupByUtility groupByUtility, ResultUtility resultUtility, InputProcessor inputProcessor) {
    this.groupByUtility = groupByUtility;
    this.resultUtility = resultUtility;
    this.inputProcessor = inputProcessor;
  }

  @Override
  public MapAggregator<OSHDBCombinedIndex<OSHDBTimestamp, Integer>, OSMEntitySnapshot> compute() throws Exception {
    MapReducer<OSMEntitySnapshot> mapRed = inputProcessor.getMapReducer();
    keysInt = groupByUtility.getOSHDBKeysOfMultipleTags();
    return aggregate(mapRed);
  }

  public List<GroupByResult> getResult(SortedMap<OSHDBCombinedIndex<OSHDBTimestamp, Integer>, Number> preResult) {
    var groupByResult = ExecutionUtils.nest(preResult);
    List<GroupByResult> resultSet = new ArrayList<>();
    String groupByName = "";
    TagTranslator tt = DbConnData.tagTranslator;
    for (var entry : groupByResult.entrySet()) {
      List<Result> results = resultUtility.fillElementsResult(entry.getValue(), null, inputProcessor);
      // check for non-remainder objects (which do have the defined key)
      if (entry.getKey() != -1) {
        groupByName = tt.getOSMTagKeyOf(entry.getKey().intValue()).toString();
      } else {
        groupByName = "remainder";
      }
      resultSet.add(new GroupByResult(groupByName, results));
    }
    return resultSet;
  }

  private MapAggregator<OSHDBCombinedIndex<OSHDBTimestamp, Integer>, OSMEntitySnapshot> aggregate(
      MapReducer<OSMEntitySnapshot> mapRed) {
    return mapRed.flatMap(f -> {
      List<Pair<Integer, OSMEntitySnapshot>> res = new LinkedList<>();
      Iterable<OSHDBTag> tags = f.getEntity().getTags();
      for (OSHDBTag tag : tags) {
        int tagKeyId = tag.getKey();
        for (int key : keysInt) {
          if (tagKeyId == key) {
            res.add(new ImmutablePair<>(tagKeyId, f));
          }
        }
      }
      if (res.isEmpty()) {
        res.add(new ImmutablePair<>(-1, f));
      }
      return res;
    }).aggregateByTimestamp().aggregateBy(Pair::getKey, Arrays.asList(keysInt))
        .map(Pair::getValue);
  }

  @Override
  public Response getResponse(List resultSet) {
    return new GroupByResponse(resultSet, this);
  }

  @Override
  public String getDescription() {
    return "";
  }

  @Override
  public String getUnit() {
    return "";
  }

  @Override
  public String getMetadataDescription(){
    return Description.aggregateGroupByKey(this.getDescription(),
        this.getUnit());
  }
}
