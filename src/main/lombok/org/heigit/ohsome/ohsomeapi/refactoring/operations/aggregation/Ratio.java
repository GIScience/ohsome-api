package org.heigit.ohsome.ohsomeapi.refactoring.operations.aggregation;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.SortedMap;
import javax.servlet.http.HttpServletRequest;
import org.heigit.ohsome.ohsomeapi.executor.ExecutionUtils;
import org.heigit.ohsome.ohsomeapi.executor.ExecutionUtils.MatchType;
import org.heigit.ohsome.ohsomeapi.inputprocessing.InputProcessingUtils;
import org.heigit.ohsome.ohsomeapi.inputprocessing.InputProcessor;
import org.heigit.ohsome.ohsomeapi.oshdb.DbConnData;
import org.heigit.ohsome.ohsomeapi.output.Response;
import org.heigit.ohsome.ohsomeapi.output.ratio.RatioGroupByResult;
import org.heigit.ohsome.ohsomeapi.output.ratio.RatioResponse;
import org.heigit.ohsome.ohsomeapi.output.ratio.RatioResult;
import org.heigit.ohsome.ohsomeapi.refactoring.operations.Operation;
import org.heigit.ohsome.ohsomeapi.refactoring.operations.SnapshotView;
import org.heigit.ohsome.ohsomeapi.utilities.DecimalFormatDefiner;
import org.heigit.ohsome.ohsomeapi.utilities.FilterUtility;
import org.heigit.ohsome.oshdb.OSHDBTimestamp;
import org.heigit.ohsome.oshdb.api.generic.OSHDBCombinedIndex;
import org.heigit.ohsome.oshdb.api.mapreducer.MapAggregator;
import org.heigit.ohsome.oshdb.api.mapreducer.MapReducer;
import org.heigit.ohsome.oshdb.filter.FilterExpression;
import org.heigit.ohsome.oshdb.filter.FilterParser;
import org.heigit.ohsome.oshdb.osm.OSMEntity;
import org.heigit.ohsome.oshdb.util.mappable.OSMEntitySnapshot;
import org.heigit.ohsome.oshdb.util.time.TimestampFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Ratio implements Operation<MapAggregator>, SnapshotView {

  @Autowired
  InputProcessor inputProcessor;
  @Autowired
  SnapshotView snapshotView;
  @Autowired
  HttpServletRequest servletRequest;
  @Autowired
  DecimalFormatDefiner decimalFormatDefiner;
  final DecimalFormat decimalFormat = decimalFormatDefiner.getDecimalFormatForRatioRequests();
  @Autowired
  FilterUtility filterUtility;
  @Autowired
  InputProcessingUtils inputProcessingUtils;

  @Override
  public MapAggregator compute() throws Exception {
    MapReducer<OSMEntitySnapshot> mapRed = inputProcessor.processParameters(snapshotView);
    String combinedFilter = filterUtility.combineFiltersWithOr(inputProcessor.getFilter(), inputProcessor.getFilter2());
    inputProcessor.setFilter(combinedFilter);
    mapRed = mapRed.filter(combinedFilter);
   return aggregate(mapRed.aggregateByTimestamp());
    //the call to computeResult should be done in the controller e.g. count.countGroupBy(mapAggregator)
    //countGroupBy functions should be renamed in Count, Length, Perimeter and Area
    // since it is called not only in case of groupBy resources
    // var result = ExecutionUtils.computeResult(operation, preResult);
  }

  private MapAggregator<OSHDBCombinedIndex<OSHDBTimestamp, ExecutionUtils.MatchType>, OSMEntitySnapshot> aggregate(MapAggregator<OSHDBTimestamp, OSMEntitySnapshot> mapAggregator) {
    filterUtility.checkFilter(inputProcessor.getFilter2());
    FilterParser fp = new FilterParser(DbConnData.tagTranslator);
    FilterExpression filterExpr1 = filterUtility.parseFilter(fp, inputProcessor.getFilter());
    FilterExpression filterExpr2 = filterUtility.parseFilter(fp, inputProcessor.getFilter2());
    var preResult = mapAggregator.aggregateBy(snapshot -> {
      OSMEntity entity = snapshot.getEntity();
      boolean matches1 = filterExpr1.applyOSMGeometry(entity, snapshot::getGeometry);
      boolean matches2 = filterExpr2.applyOSMGeometry(entity, snapshot::getGeometry);
      if (matches1 && matches2) {
        return MatchType.MATCHESBOTH;
      } else if (matches1) {
        return MatchType.MATCHES1;
      } else if (matches2) {
        return MatchType.MATCHES2;
      } else {
        // this should never be reached
        assert false : "MatchType matches none.";
        return MatchType.MATCHESNONE;
      }
    }, EnumSet.allOf(MatchType.class));
    return preResult;
  }

  public List<RatioResult> getResult(SortedMap<OSHDBCombinedIndex<OSHDBTimestamp, MatchType>, Number> preResult) {
    int resultSize = preResult.size();
    int matchTypeSize = 4;
    Double[] value1 = new Double[resultSize / matchTypeSize];
    Double[] value2 = new Double[resultSize / matchTypeSize];
    String[] timeArray = new String[resultSize / matchTypeSize];
    int value1Count = 0;
    int value2Count = 0;
    int matchesBothCount = 0;
    // time and value extraction
    for (var entry : preResult.entrySet()) {
      if (entry.getKey().getSecondIndex() == MatchType.MATCHES2) {
        timeArray[value2Count] =
            TimestampFormatter.getInstance().isoDateTime(entry.getKey().getFirstIndex());
        value2[value2Count] = Double.parseDouble(decimalFormat.format(entry.getValue().doubleValue()));
        value2Count++;
      }
      if (entry.getKey().getSecondIndex() == MatchType.MATCHES1) {
        value1[value1Count] = Double.parseDouble(decimalFormat.format(entry.getValue().doubleValue()));
        value1Count++;
      }
      if (entry.getKey().getSecondIndex() == MatchType.MATCHESBOTH) {
        value1[matchesBothCount] = value1[matchesBothCount]
            + Double.parseDouble(decimalFormat.format(entry.getValue().doubleValue()));
        value2[matchesBothCount] = value2[matchesBothCount]
            + Double.parseDouble(decimalFormat.format(entry.getValue().doubleValue()));
        matchesBothCount++;
      }
    }
    List<RatioResult> resultSet = new ArrayList<>();
    for (int i = 0; i < timeArray.length; i++) {
      double ratio = value2[i] / value1[i];
      // in case ratio has the values "NaN", "Infinity", etc.
      try {
        ratio = Double.parseDouble(decimalFormat.format(ratio));
      } catch (Exception e) {
        // do nothing --> just return ratio without rounding (trimming)
      }
      resultSet.add(new RatioResult(timeArray[i], value1[i], value2[i], ratio));
    }
    return resultSet;
  }

  public RatioGroupByResult[] getRatioGroupBy(String[] timeArray, Double[] resultValues1, Double[] resultValues2) {
    Object[] boundaryIds = inputProcessingUtils.getBoundaryIds();
    int boundaryIdsLength = boundaryIds.length;
    int timeArrayLenth = timeArray.length;
    RatioGroupByResult[] groupByResultSet = new RatioGroupByResult[boundaryIdsLength];
    for (int i = 0; i < boundaryIdsLength; i++) {
      Object groupByName = boundaryIds[i];
      RatioResult[] ratioResultSet = new RatioResult[timeArrayLenth];
      int innerCount = 0;
      for (int j = i; j < timeArrayLenth * boundaryIdsLength; j += boundaryIdsLength) {
        double ratio = resultValues2[j] / resultValues1[j];
        // in case ratio has the values "NaN", "Infinity", etc.
        try {
          ratio = Double.parseDouble(decimalFormat.format(ratio));
        } catch (Exception e) {
          // do nothing --> just return ratio without rounding (trimming)
        }
        ratioResultSet[innerCount] =
            new RatioResult(timeArray[innerCount], resultValues1[j], resultValues2[j], ratio);
        innerCount++;
      }
      groupByResultSet[i] = new RatioGroupByResult(groupByName, ratioResultSet);
    }
    return groupByResultSet;
  }

  @Override
  public Response getResponse(List<RatioResult> result) {
    return new RatioResponse(result);
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
