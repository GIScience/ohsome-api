package org.heigit.ohsome.ohsomeapi.refactoring.operations.aggregation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.SortedMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.heigit.ohsome.ohsomeapi.exception.BadRequestException;
import org.heigit.ohsome.ohsomeapi.executor.ExecutionUtils;
import org.heigit.ohsome.ohsomeapi.geometrybuilders.GeometryFrom;
import org.heigit.ohsome.ohsomeapi.inputprocessing.InputProcessingUtils;
import org.heigit.ohsome.ohsomeapi.inputprocessing.InputProcessor;
import org.heigit.ohsome.ohsomeapi.output.Response;
import org.heigit.ohsome.ohsomeapi.output.Result;
import org.heigit.ohsome.ohsomeapi.output.groupby.GroupByResponse;
import org.heigit.ohsome.ohsomeapi.output.groupby.GroupByResult;
import org.heigit.ohsome.ohsomeapi.refactoring.operations.Operation;
import org.heigit.ohsome.ohsomeapi.refactoring.operations.SnapshotView;
import org.heigit.ohsome.ohsomeapi.utilities.ResultUtility;
import org.heigit.ohsome.oshdb.OSHDBTimestamp;
import org.heigit.ohsome.oshdb.api.generic.OSHDBCombinedIndex;
import org.heigit.ohsome.oshdb.api.mapreducer.MapAggregator;
import org.heigit.ohsome.oshdb.api.mapreducer.MapReducer;
import org.heigit.ohsome.oshdb.filter.FilterExpression;
import org.heigit.ohsome.oshdb.util.function.SerializableFunction;
import org.heigit.ohsome.oshdb.util.geometry.Geo;
import org.heigit.ohsome.oshdb.util.mappable.OSMEntitySnapshot;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Polygonal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GroupByBoundary extends Group implements Operation, SnapshotView {

  @Autowired
  InputProcessor inputProcessor;
  @Autowired
  SnapshotView snapshotView;
  @Autowired
  InputProcessingUtils inputProcessingUtils;
  @Autowired
  GroupByResponse groupByResponse;
  @Autowired
  ResultUtility resultUtility;
  @Autowired
  GeometryFrom geometryFrom;

  public List compute() throws Exception {
    inputProcessor.getProcessingData().setGroupByBoundary(true);
    MapReducer<OSMEntitySnapshot> mapRed = inputProcessor.processParameters(snapshotView);
    var result = computeCountLengthPerimeterAreaGbB(this, mapRed);
    SortedMap<Integer, ? extends SortedMap<OSHDBTimestamp, ? extends Number>> groupByResult;
    groupByResult = ExecutionUtils.nest(result);
    List<GroupByResult> resultSet = new ArrayList<>();
    Object groupByName;
    Object[] boundaryIds = inputProcessingUtils.getBoundaryIds();
    int count = 0;
    ArrayList<Geometry> boundaries = new ArrayList<>(inputProcessor.getProcessingData().getBoundaryList());
    for (Entry<Integer, ? extends SortedMap<OSHDBTimestamp, ? extends Number>> entry : groupByResult
        .entrySet()) {
      List<Result> results = resultUtility.fillElementsResult(entry.getValue(), inputProcessor.isDensity(),
          boundaries.get(count));
      groupByName = boundaryIds[count];
      resultSet.add(new GroupByResult(groupByName, results));
      count++;
    }
    return resultSet;
  }

  /**
   * Computes the result for the /count|length|perimeter|area/groupBy/boundary resources.
   *
   * @throws BadRequestException if a boundary parameter is not defined.
   * @throws Exception thrown by
   *         {@link org.heigit.ohsome.oshdb.api.mapreducer.MapAggregator#count() count}, or
   *         {@link org.heigit.ohsome.oshdb.api.mapreducer.MapAggregator#sum(SerializableFunction)
   *         sum}
   */
  private <P extends Geometry & Polygonal> SortedMap<OSHDBCombinedIndex<OSHDBTimestamp, Integer>,
      ? extends Number> computeCountLengthPerimeterAreaGbB(Operation operation, MapReducer<OSMEntitySnapshot> mapRed) throws Exception {
    List<Geometry> arrGeoms = new ArrayList<>(geometryFrom.getGeometryList());
    @SuppressWarnings("unchecked") // intentionally as check for P on Polygonal is already performed
    Map<Integer, P> geoms = IntStream.range(0, arrGeoms.size()).boxed()
        .collect(Collectors.toMap(idx -> idx, idx -> (P) arrGeoms.get(idx)));
    MapAggregator<OSHDBCombinedIndex<OSHDBTimestamp, Integer>, OSMEntitySnapshot> mapAgg =
        mapRed.aggregateByTimestamp().aggregateByGeometry(geoms);
    if (inputProcessor.getProcessingData().isContainingSimpleFeatureTypes()) {
      mapAgg = inputProcessor.filterOnSimpleFeatures(mapAgg);
    }
    Optional<FilterExpression> filter = inputProcessor.getProcessingData().getFilterExpression();
    if (filter.isPresent()) {
      mapAgg = mapAgg.filter(filter.get());
    }
    var mapAggGeom = mapAgg.map(OSMEntitySnapshot::getGeometry);
    if (operation instanceof Count) {
      return mapAgg.count();
    } else if (operation instanceof Perimeter) {
      return mapAggGeom.sum(geom -> {
        if (!(geom instanceof Polygonal)) {
          return 0.0;
        }
        return ExecutionUtils.cacheInUserData(geom, () -> Geo.lengthOf(geom.getBoundary()));
      });
    } else if (operation instanceof Length) {
      return mapAggGeom
          .sum(geom -> ExecutionUtils.cacheInUserData(geom, () -> Geo.lengthOf(geom)));
    } else if (operation instanceof Area) {
      return mapAggGeom.sum(geom -> ExecutionUtils.cacheInUserData(geom, () -> Geo.areaOf(geom)));
    } else {
      return null;
    }
  }

  @Override
  public Response getResponse(List resultSet){
    return groupByResponse.getResponse(this, resultSet);
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
