package org.heigit.ohsome.ohsomeapi.refactoring.operations.aggregation;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.SortedMap;
import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import org.heigit.ohsome.ohsomeapi.executor.ExecutionUtils.MatchType;
import org.heigit.ohsome.ohsomeapi.inputprocessing.InputProcessingUtils;
import org.heigit.ohsome.ohsomeapi.inputprocessing.InputProcessor;
import org.heigit.ohsome.ohsomeapi.oshdb.DbConnData;
import org.heigit.ohsome.ohsomeapi.output.Description;
import org.heigit.ohsome.ohsomeapi.output.RatioDataStructure;
import org.heigit.ohsome.ohsomeapi.output.Response;
import org.heigit.ohsome.ohsomeapi.output.ratio.RatioGroupByBoundaryResponse;
import org.heigit.ohsome.ohsomeapi.output.ratio.RatioGroupByResult;
import org.heigit.ohsome.ohsomeapi.output.ratio.RatioResponse;
import org.heigit.ohsome.ohsomeapi.output.ratio.RatioResult;
import org.heigit.ohsome.ohsomeapi.refactoring.operations.Operation;
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
public class Ratio implements Operation {

  @Autowired
  private InputProcessor inputProcessor;
  @Autowired
  DecimalFormatDefiner decimalFormatDefiner;
  @Autowired
  FilterUtility filterUtility;
  @Autowired
  InputProcessingUtils inputProcessingUtils;
  @Autowired
  HttpServletRequest servletRequest;
  private DecimalFormat decimalFormat;

  @PostConstruct
  public void init() {
     decimalFormat = decimalFormatDefiner.getDecimalFormatForRatioRequests();
  }

  @Override
  public MapReducer<OSMEntitySnapshot> compute() throws Exception {
    MapReducer<OSMEntitySnapshot> mapRed = inputProcessor.getMapReducer();
    String combinedFilter = filterUtility.combineFiltersWithOr(inputProcessor.getFilter(),
    inputProcessor.getFilter2());
    inputProcessor.filterMapReducer(combinedFilter);
    return mapRed.filter(combinedFilter);
  }

  public MapAggregator<? extends Comparable, ? extends Comparable> aggregateByFilterMatching(MapAggregator<OSHDBTimestamp,
      OSMEntitySnapshot> mapAggregator) {
    filterUtility.checkFilter(inputProcessor.getFilter2());
    FilterParser fp = new FilterParser(DbConnData.tagTranslator);
    FilterExpression filterExpr1 = filterUtility.parseFilter(fp, inputProcessor.getFilter());
    FilterExpression filterExpr2 = filterUtility.parseFilter(fp, inputProcessor.getFilter2());
    return mapAggregator.aggregateBy(snapshot -> {
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
  }

  public RatioDataStructure getValues(SortedMap<OSHDBCombinedIndex<OSHDBTimestamp, MatchType>, ? extends Number> preResult) {
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
    return new RatioDataStructure(timeArray, value1, value2);
  }

  public List<RatioResult> getRatioResult(RatioDataStructure ratioDataStructure) {
    List<RatioResult> resultSet = new ArrayList<>();
    for (int i = 0; i < ratioDataStructure.getTimeArray().length; i++) {
      double ratio = ratioDataStructure.getValue2()[i] / ratioDataStructure.getValue1()[i];
      // in case ratio has the values "NaN", "Infinity", etc.
      try {
        ratio = Double.parseDouble(decimalFormat.format(ratio));
      } catch (Exception e) {
        // do nothing --> just return ratio without rounding (trimming)
      }
      resultSet.add(new RatioResult(ratioDataStructure.getTimeArray()[i], ratioDataStructure.getValue1()[i], ratioDataStructure.getValue2()[i], ratio));
    }
    return resultSet;
  }

  public List<RatioGroupByResult> getRatioGroupByResult(RatioDataStructure ratioDataStructure) {
    Object[] boundaryIds = inputProcessingUtils.getBoundaryIds();
    int boundaryIdsLength = boundaryIds.length;
    int timeArrayLength = ratioDataStructure.getTimeArray().length;
    List<RatioGroupByResult> groupByResultSet = new ArrayList<>();
    for (int i = 0; i < boundaryIdsLength; i++) {
      Object groupByName = boundaryIds[i];
      RatioResult[] ratioResultSet = new RatioResult[timeArrayLength];
      int innerCount = 0;
      for (int j = i; j < timeArrayLength * boundaryIdsLength; j += boundaryIdsLength) {
        double ratio = ratioDataStructure.getValue2()[j] / ratioDataStructure.getValue1()[j];
        // in case ratio has the values "NaN", "Infinity", etc.
        try {
          ratio = Double.parseDouble(decimalFormat.format(ratio));
        } catch (Exception e) {
          // do nothing --> just return ratio without rounding (trimming)
        }
        ratioResultSet[innerCount] =
            new RatioResult(ratioDataStructure.getTimeArray()[innerCount], ratioDataStructure.getValue1()[j], ratioDataStructure.getValue2()[j], ratio);
        innerCount++;
      }
      groupByResultSet.add(new RatioGroupByResult(groupByName, ratioResultSet));
    }
    return groupByResultSet;
  }

  @Override
  public Response getResponse(List result) {
    if (servletRequest.getRequestURL().toString().contains("groupBy/boundary")) {
      new RatioGroupByBoundaryResponse(result, this);
    }
    return new RatioResponse(result, this);
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
    if (servletRequest.getRequestURL().toString().contains("groupBy/boundary")) {
      return Description.aggregateRatioGroupByBoundary(this.getDescription(), this.getUnit());
    }
    return Description.aggregateRatio(this.getDescription(), this.getUnit());
  }

  @Override
  public InputProcessor getInputProcessor() {
    return inputProcessor;
  }
}
