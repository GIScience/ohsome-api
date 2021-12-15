package org.heigit.ohsome.ohsomeapi.refactoring.operations.aggregation;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.heigit.ohsome.ohsomeapi.exception.BadRequestException;
import org.heigit.ohsome.ohsomeapi.exception.ExceptionMessages;
import org.heigit.ohsome.ohsomeapi.executor.ExecutionUtils;
import org.heigit.ohsome.ohsomeapi.inputprocessing.InputProcessingUtils;
import org.heigit.ohsome.ohsomeapi.inputprocessing.InputProcessor;
import org.heigit.ohsome.ohsomeapi.oshdb.DbConnData;
import org.heigit.ohsome.ohsomeapi.output.DefaultAggregationResponse;
import org.heigit.ohsome.ohsomeapi.output.Response;
import org.heigit.ohsome.ohsomeapi.output.Result;
import org.heigit.ohsome.ohsomeapi.output.groupby.GroupByResult;
import org.heigit.ohsome.ohsomeapi.refactoring.operations.Operation;
import org.heigit.ohsome.ohsomeapi.refactoring.operations.SnapshotView;
import org.heigit.ohsome.ohsomeapi.utilities.ResultUtility;
import org.heigit.ohsome.oshdb.api.mapreducer.MapReducer;
import org.heigit.ohsome.oshdb.filter.FilterExpression;
import org.heigit.ohsome.oshdb.filter.FilterParser;
import org.heigit.ohsome.oshdb.util.mappable.OSMEntitySnapshot;
import org.heigit.ohsome.oshdb.util.tagtranslator.TagTranslator;
import org.locationtech.jts.geom.Geometry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GroupByTag extends Group implements Operation, SnapshotView {

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
  @Autowired
  InputProcessingUtils inputProcessingUtils;

  @Override
  public List compute() throws Exception {
    int keysInt = this.getOSHDBTagKey();
    Integer[] valuesInt = this.getOSHDBTag();
    List<Pair<Integer, Integer>> zeroFill = this.getListOfKeyValuePair(keysInt, valuesInt);
    MapReducer<OSMEntitySnapshot> mapRed = inputProcessor.processParameters(snapshotView);
    var preResult = mapRed.map(f -> ExecutionUtils.mapSnapshotToTags(keysInt, valuesInt, f))
        .aggregateByTimestamp().aggregateBy(Pair::getKey, zeroFill).map(Pair::getValue);
    var result = ExecutionUtils.computeResult(this, preResult);
    var groupByResult = ExecutionUtils.nest(result);
    List<GroupByResult> resultSet = new ArrayList<>();
    String groupByName = "";
    Geometry geom = inputProcessor.getGeometry();
    TagTranslator tt = DbConnData.tagTranslator;
    int count = 0;
    for (var entry : groupByResult.entrySet()) {
      List<Result> results = resultUtility.fillElementsResult(entry.getValue(),
          inputProcessor.isDensity(), geom);
      // check for non-remainder objects (which do have the defined key and value)
      if (entry.getKey().getKey() != -1 && entry.getKey().getValue() != -1) {
        groupByName = tt.getOSMTagOf(keysInt, entry.getKey().getValue()).toString();
      } else {
        groupByName = "remainder";
      }
      resultSet.add(new GroupByResult(groupByName, results));
      count++;
    }
    // used to remove null objects from the resultSet
    resultSet = resultSet.stream().filter(x -> Objects.nonNull(x)).collect(Collectors.toList());
    return resultSet;
  }

  public int getOSHDBTagKey() throws Exception {
    String[] groupByKey = inputProcessor.splitParamOnComma(
        inputProcessor.createEmptyArrayIfNull(servletRequest.getParameterValues("groupByKey")));
    if (groupByKey.length != 1) {
      throw new BadRequestException(ExceptionMessages.GROUP_BY_KEY_PARAM);
    }
    TagTranslator tt = DbConnData.tagTranslator;
    int keysInt = tt.getOSHDBTagKeyOf(groupByKey[0]).toInt();
    return keysInt;
  }

  public Integer[] getOSHDBTag() {
    String[] groupByKey = inputProcessor.splitParamOnComma(
        inputProcessor.createEmptyArrayIfNull(servletRequest.getParameterValues("groupByKey")));
    if (groupByKey.length != 1) {
      throw new BadRequestException(ExceptionMessages.GROUP_BY_KEY_PARAM);
    }
    String[] groupByValues = inputProcessor.splitParamOnComma(
        inputProcessor.createEmptyArrayIfNull(servletRequest.getParameterValues("groupByValues")));
    Integer[] valuesInt = new Integer[groupByValues.length];
    TagTranslator tt = DbConnData.tagTranslator;
    if (groupByValues.length != 0) {
      for (int j = 0; j < groupByValues.length; j++) {
        valuesInt[j] = tt.getOSHDBTagOf(groupByKey[0], groupByValues[j]).getValue();
      }
    }
    return valuesInt;
  }

  public List<Pair<Integer, Integer>> getListOfKeyValuePair(int keysInt, Integer[] valuesInt) {
    ArrayList<Pair<Integer, Integer>> zeroFill = new ArrayList<>();
    String[] groupByValues = inputProcessor.splitParamOnComma(
        inputProcessor.createEmptyArrayIfNull(servletRequest.getParameterValues("groupByValues")));
    if (groupByValues.length != 0) {
      for (int j = 0; j < groupByValues.length; j++) {
        zeroFill.add(new ImmutablePair<>(keysInt, valuesInt[j]));
      }
    }
    return zeroFill;
  }

  private void computeThroughFilters() throws Exception {
    MapReducer<OSMEntitySnapshot> mapRed = null;
    mapRed = inputProcessor.processParameters(snapshotView);
    String[] groupByKey = inputProcessor.splitParamOnComma(
        inputProcessor.createEmptyArrayIfNull(servletRequest.getParameterValues("groupByKey")));
    if (groupByKey.length != 1) {
      throw new BadRequestException(ExceptionMessages.GROUP_BY_KEY_PARAM);
    }
    String filter = groupByKey[0] + "=*";
    FilterParser fp = new FilterParser(DbConnData.tagTranslator);
    FilterExpression filterExpr = inputProcessingUtils.parseFilter(fp, filter);
      mapRed = mapRed.filter(filterExpr);
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
