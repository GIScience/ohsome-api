package org.heigit.ohsome.ohsomeapi.refactoring.operations.aggregation;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.SortedMap;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.tuple.Pair;
import org.heigit.ohsome.ohsomeapi.exception.BadRequestException;
import org.heigit.ohsome.ohsomeapi.exception.ExceptionMessages;
import org.heigit.ohsome.ohsomeapi.executor.ExecutionUtils;
import org.heigit.ohsome.ohsomeapi.inputprocessing.InputProcessingUtils;
import org.heigit.ohsome.ohsomeapi.inputprocessing.InputProcessor;
import org.heigit.ohsome.ohsomeapi.oshdb.DbConnData;
import org.heigit.ohsome.ohsomeapi.output.Description;
import org.heigit.ohsome.ohsomeapi.output.Response;
import org.heigit.ohsome.ohsomeapi.output.Result;
import org.heigit.ohsome.ohsomeapi.output.groupby.GroupByResponse;
import org.heigit.ohsome.ohsomeapi.output.groupby.GroupByResult;
import org.heigit.ohsome.ohsomeapi.refactoring.operations.Operation;
import org.heigit.ohsome.ohsomeapi.utilities.ResultUtility;
import org.heigit.ohsome.oshdb.OSHDBTimestamp;
import org.heigit.ohsome.oshdb.api.generic.OSHDBCombinedIndex;
import org.heigit.ohsome.oshdb.api.mapreducer.MapAggregator;
import org.heigit.ohsome.oshdb.api.mapreducer.MapReducer;
import org.heigit.ohsome.oshdb.filter.FilterExpression;
import org.heigit.ohsome.oshdb.filter.FilterParser;
import org.heigit.ohsome.oshdb.util.mappable.OSMEntitySnapshot;
import org.heigit.ohsome.oshdb.util.tagtranslator.TagTranslator;
import org.locationtech.jts.geom.Geometry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GroupByTag extends Group implements Operation {

  @Autowired
  HttpServletRequest servletRequest;
  @Autowired
  ResultUtility resultUtility;
  @Autowired
  InputProcessingUtils inputProcessingUtils;
  private int keysInt;
  @Autowired
  private InputProcessor inputProcessor;

  @Override
  public MapAggregator<OSHDBCombinedIndex<OSHDBTimestamp, Pair<Integer, Integer>>, OSMEntitySnapshot> compute() throws Exception {
    MapReducer<OSMEntitySnapshot> mapRed = inputProcessor.getMapReducer();
    keysInt = getOSHDBKeyOfOneTag();
    Integer[] valuesInt = getOSHDBTag();
    List<Pair<Integer, Integer>> zeroFill = this.getListOfKeyValuePair(keysInt, valuesInt);
    return aggregate(mapRed, keysInt, valuesInt, zeroFill);
  }

  public List<GroupByResult> getResult(SortedMap<OSHDBCombinedIndex<OSHDBTimestamp, Pair<Integer, Integer>>, Number> preResult) {
    var groupByResult = nest(preResult);
    List<GroupByResult> resultSet = new ArrayList<>();
    String groupByName = "";
    Geometry geom = inputProcessor.getGeometry();
    TagTranslator tt = DbConnData.tagTranslator;
    for (var entry : groupByResult.entrySet()) {
      List<Result> results = resultUtility.fillElementsResult(entry.getValue(), geom, inputProcessor);
      // check for non-remainder objects (which do have the defined key and value)
      if (entry.getKey().getKey() != -1 && entry.getKey().getValue() != -1) {
        groupByName = tt.getOSMTagOf(keysInt, entry.getKey().getValue()).toString();
      } else {
        groupByName = "remainder";
      }
      resultSet.add(new GroupByResult(groupByName, results));
    }
    // used to remove null objects from the resultSet
    resultSet = resultSet.stream().filter(Objects::nonNull).collect(Collectors.toList());
    return resultSet;
  }

  private MapAggregator<OSHDBCombinedIndex<OSHDBTimestamp, Pair<Integer, Integer>>, OSMEntitySnapshot> aggregate(
      MapReducer<OSMEntitySnapshot> mapRed, int keysInt, Integer[] valuesInt, List<Pair<Integer, Integer>> zeroFill) {
    return mapRed.map(f -> ExecutionUtils.mapSnapshotToTags(keysInt, valuesInt, f))
        .aggregateByTimestamp().aggregateBy(Pair::getKey, zeroFill).map(Pair::getValue);
  }

  private void computeThroughFilters() throws Exception {
    MapReducer<OSMEntitySnapshot> mapRed = inputProcessor.getMapReducer();
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
  public String getMetadataDescription() {
    return Description.aggregateGroupByTag(inputProcessor.isDensity(),
        this.getDescription(), this.getUnit());
  }

  @Override
  public InputProcessor getInputProcessor() {
    return inputProcessor;
  }
}
