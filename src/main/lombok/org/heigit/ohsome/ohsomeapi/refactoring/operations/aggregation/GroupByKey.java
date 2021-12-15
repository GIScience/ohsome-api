package org.heigit.ohsome.ohsomeapi.refactoring.operations.aggregation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.heigit.ohsome.ohsomeapi.exception.BadRequestException;
import org.heigit.ohsome.ohsomeapi.exception.ExceptionMessages;
import org.heigit.ohsome.ohsomeapi.executor.ExecutionUtils;
import org.heigit.ohsome.ohsomeapi.inputprocessing.InputProcessor;
import org.heigit.ohsome.ohsomeapi.inputprocessing.ProcessingData;
import org.heigit.ohsome.ohsomeapi.oshdb.DbConnData;
import org.heigit.ohsome.ohsomeapi.output.DefaultAggregationResponse;
import org.heigit.ohsome.ohsomeapi.output.Response;
import org.heigit.ohsome.ohsomeapi.output.Result;
import org.heigit.ohsome.ohsomeapi.output.groupby.GroupByResult;
import org.heigit.ohsome.ohsomeapi.refactoring.operations.Operation;
import org.heigit.ohsome.ohsomeapi.refactoring.operations.SnapshotView;
import org.heigit.ohsome.ohsomeapi.utilities.ResultUtility;
import org.heigit.ohsome.oshdb.OSHDBTag;
import org.heigit.ohsome.oshdb.OSHDBTimestamp;
import org.heigit.ohsome.oshdb.api.generic.OSHDBCombinedIndex;
import org.heigit.ohsome.oshdb.api.mapreducer.MapAggregator;
import org.heigit.ohsome.oshdb.api.mapreducer.MapReducer;
import org.heigit.ohsome.oshdb.util.mappable.OSMEntitySnapshot;
import org.heigit.ohsome.oshdb.util.tagtranslator.TagTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GroupByKey extends Group implements Operation, SnapshotView {

  @Autowired
  InputProcessor inputProcessor;
  @Autowired
  HttpServletRequest servletRequest;
  @Autowired
  SnapshotView snapshotView;
  @Autowired
  ResultUtility resultUtility;
  @Autowired
  DefaultAggregationResponse defaultAggregationResponse;

  @Override
  public List compute() throws Exception {
    MapReducer<OSMEntitySnapshot> mapRed = null;
    String[] groupByKeys = inputProcessor.splitParamOnComma(
        inputProcessor.createEmptyArrayIfNull(servletRequest.getParameterValues("groupByKeys")));
    if (groupByKeys == null || groupByKeys.length == 0) {
      throw new BadRequestException(ExceptionMessages.GROUP_BY_KEYS_PARAM);
    }
    mapRed = inputProcessor.processParameters(snapshotView);
    ProcessingData processingData = inputProcessor.getProcessingData();
    //RequestParameters requestParameters = processingData.getRequestParameters();
    TagTranslator tt = DbConnData.tagTranslator;
    Integer[] keysInt = new Integer[groupByKeys.length];
    for (int i = 0; i < groupByKeys.length; i++) {
      keysInt[i] = tt.getOSHDBTagKeyOf(groupByKeys[i]).toInt();
    }
    MapAggregator<OSHDBCombinedIndex<OSHDBTimestamp, Integer>, OSMEntitySnapshot> preResult =
        mapRed.flatMap(f -> {
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
    var result = ExecutionUtils.computeResult(this, preResult);
    var groupByResult = ExecutionUtils.nest(result);
    List<GroupByResult> resultSet = new ArrayList<>();
    String groupByName = "";
    int count = 0;
    for (var entry : groupByResult.entrySet()) {
      List<Result> results = resultUtility.fillElementsResult(entry.getValue(),
          inputProcessor.isDensity(), null);
      // check for non-remainder objects (which do have the defined key)
      if (entry.getKey() != -1) {
        groupByName = tt.getOSMTagKeyOf(entry.getKey().intValue()).toString();
      } else {
        groupByName = "remainder";
      }
      resultSet.add(new GroupByResult(groupByName, results));
      count++;
    }
    return resultSet;
  }

  @Override
  public Response getResponse(List resultSet) {
    return defaultAggregationResponse.getResponse(this, resultSet);
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
