package org.heigit.ohsome.ohsomeapi.refactoring.operations.aggregation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.SortedMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.Getter;
import org.apache.commons.lang3.tuple.Pair;
import org.heigit.ohsome.ohsomeapi.executor.ExecutionUtils;
import org.heigit.ohsome.ohsomeapi.inputprocessing.InputProcessor;
import org.heigit.ohsome.ohsomeapi.oshdb.DbConnData;
import org.heigit.ohsome.ohsomeapi.output.Description;
import org.heigit.ohsome.ohsomeapi.output.Response;
import org.heigit.ohsome.ohsomeapi.output.Result;
import org.heigit.ohsome.ohsomeapi.output.elements.ElementsResult;
import org.heigit.ohsome.ohsomeapi.output.groupby.GroupByResponse;
import org.heigit.ohsome.ohsomeapi.output.groupby.GroupByResult;
import org.heigit.ohsome.ohsomeapi.refactoring.operations.Operation;
import org.heigit.ohsome.ohsomeapi.utilities.SpatialUtility;
import org.heigit.ohsome.oshdb.OSHDBTimestamp;
import org.heigit.ohsome.oshdb.api.generic.OSHDBCombinedIndex;
import org.heigit.ohsome.oshdb.api.mapreducer.MapAggregator;
import org.heigit.ohsome.oshdb.api.mapreducer.MapReducer;
import org.heigit.ohsome.oshdb.filter.FilterExpression;
import org.heigit.ohsome.oshdb.util.mappable.OSMEntitySnapshot;
import org.heigit.ohsome.oshdb.util.tagtranslator.TagTranslator;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Polygonal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.RequestScope;

@Service
@RequestScope
public class GroupByBoundaryGroupByTag implements Operation {

  private final SpatialUtility spatialUtility;
  private int keysInt;
  @Getter
  private final InputProcessor inputProcessor;
  private final  Group group;

  @Autowired
  public GroupByBoundaryGroupByTag(Group group, SpatialUtility spatialUtility,
      InputProcessor inputProcessor) {
    this.group = group;
    this.spatialUtility = spatialUtility;
    this.inputProcessor = inputProcessor;
  }

  public MapAggregator<OSHDBCombinedIndex<OSHDBCombinedIndex<Integer, Pair<Integer, Integer>>, OSHDBTimestamp>, OSMEntitySnapshot> compute() throws Exception {
    keysInt = group.getOSHDBKeyOfOneTag();
    Integer[] valuesInt = group.getOSHDBTag();
    List<Pair<Integer, Integer>> zeroFill = group.getListOfKeyValuePair(keysInt, valuesInt);
    MapReducer<OSMEntitySnapshot> mapRed = inputProcessor.getMapReducer();
    return aggregate(mapRed, keysInt, valuesInt, zeroFill);
  }


  public List<Result> getResult(SortedMap<OSHDBCombinedIndex<OSHDBCombinedIndex<Integer, Pair<Integer, Integer>>, OSHDBTimestamp>, Number> sortedMap) {
    var groupByResult = OSHDBCombinedIndex.nest(sortedMap);
    List<Result> resultSet = new ArrayList<>();
    Object[] boundaryIds = spatialUtility.getBoundaryIds();
    ArrayList<Geometry> boundaries =
        new ArrayList<>(inputProcessor.getProcessingData().getBoundaryList());
    TagTranslator tt = DbConnData.tagTranslator;
    for (var entry : groupByResult.entrySet()) {
      int boundaryIdentifier = entry.getKey().getFirstIndex();
      List<ElementsResult> results = group.fillElementsResult(entry.getValue(),
          inputProcessor.isDensity(),
          boundaries.get(boundaryIdentifier));
      int tagValue = entry.getKey().getSecondIndex().getValue();
      String tagIdentifier;
      // check for non-remainder objects (which do have the defined key and value)
      if (entry.getKey().getSecondIndex().getKey() != -1 && tagValue != -1) {
        tagIdentifier = tt.getOSMTagOf(keysInt, tagValue).toString();
      } else {
        tagIdentifier = "remainder";
      }
      resultSet.add(new GroupByResult(new Object[] {boundaryIds[boundaryIdentifier], tagIdentifier}, results));
    }
    // used to remove null objects from the resultSet
    resultSet = resultSet.stream().filter(Objects::nonNull).collect(Collectors.toList());
    return resultSet;
  }

  public <P extends Geometry & Polygonal> MapAggregator<OSHDBCombinedIndex<OSHDBCombinedIndex<Integer, Pair<Integer, Integer>>, OSHDBTimestamp>, OSMEntitySnapshot> aggregate(MapReducer<OSMEntitySnapshot> mapRed, int keysInt, Integer[] valuesInt, List<Pair<Integer, Integer>> zeroFill) {
    var boundaries = new ArrayList<>(inputProcessor.getProcessingData().getBoundaryList());
    @SuppressWarnings("unchecked") // intentionally as check for P on Polygonal is already performed
    Map<Integer, P> geometries = IntStream.range(0, boundaries.size()).boxed()
        .collect(Collectors.toMap(idx -> idx, idx -> (P) boundaries.get(idx)));
    MapAggregator<Integer, OSMEntitySnapshot> mapAgg = mapRed.aggregateByGeometry(geometries);
    if (inputProcessor.getProcessingData().isContainingSimpleFeatureTypes()) {
      mapAgg = inputProcessor.filterOnSimpleFeatures(mapAgg);
    }
    Optional<FilterExpression> filter =
        inputProcessor.getProcessingData().getFilterExpression();
    if (filter.isPresent()) {
      mapAgg = mapAgg.filter(filter.get());
    }
    return mapAgg.map(f -> ExecutionUtils.mapSnapshotToTags(keysInt, valuesInt, f))
        .aggregateBy(Pair::getKey, zeroFill).map(Pair::getValue)
        .aggregateByTimestamp(OSMEntitySnapshot::getTimestamp);
  }

  @Override
  public String getDescription() {
    return null;
  }

  @Override
  public String getUnit() {
    return null;
  }

  @Override
  public Response getResponse(List resultSet) {
    return new GroupByResponse(resultSet, this);
  }

  @Override
  public String getMetadataDescription() {
    return Description.aggregateGroupByBoundaryGroupByTag(inputProcessor.isDensity(),
        this.getDescription(), this.getUnit());
  }
}
