package org.heigit.ohsome.ohsomeapi.refactoring.operations.aggregation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.SortedMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.Getter;
import org.heigit.ohsome.ohsomeapi.geometrybuilders.GeometryFrom;
import org.heigit.ohsome.ohsomeapi.inputprocessing.InputProcessor;
import org.heigit.ohsome.ohsomeapi.output.Description;
import org.heigit.ohsome.ohsomeapi.output.Response;
import org.heigit.ohsome.ohsomeapi.output.Result;
import org.heigit.ohsome.ohsomeapi.output.groupby.GroupByResponse;
import org.heigit.ohsome.ohsomeapi.output.groupby.GroupByResult;
import org.heigit.ohsome.ohsomeapi.refactoring.operations.Operation;
import org.heigit.ohsome.ohsomeapi.utilities.GroupByUtility;
import org.heigit.ohsome.ohsomeapi.utilities.ResultUtility;
import org.heigit.ohsome.ohsomeapi.utilities.SpatialUtility;
import org.heigit.ohsome.oshdb.OSHDBTimestamp;
import org.heigit.ohsome.oshdb.api.generic.OSHDBCombinedIndex;
import org.heigit.ohsome.oshdb.api.mapreducer.MapAggregator;
import org.heigit.ohsome.oshdb.api.mapreducer.MapReducer;
import org.heigit.ohsome.oshdb.filter.FilterExpression;
import org.heigit.ohsome.oshdb.util.mappable.OSMEntitySnapshot;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Polygonal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.RequestScope;

@Service
@RequestScope
public class GroupByBoundary implements Operation {

  private final ResultUtility resultUtility;
  private final GeometryFrom geometryFrom;
  @Getter
  private final InputProcessor inputProcessor;
  private final SpatialUtility spatialUtility;
  private  final GroupByUtility groupByUtility;

  @Autowired
  public GroupByBoundary(GroupByUtility groupByUtility, ResultUtility resultUtility, GeometryFrom geometryFrom,
      InputProcessor inputProcessor, SpatialUtility spatialUtility) {
    this.groupByUtility = groupByUtility;
    this.resultUtility = resultUtility;
    this.geometryFrom = geometryFrom;
    this.inputProcessor = inputProcessor;
    this.spatialUtility = spatialUtility;
  }

  public MapAggregator compute() throws Exception {
    inputProcessor.getProcessingData().setGroupByBoundary(true);
    MapReducer<OSMEntitySnapshot> mapRed = inputProcessor.getMapReducer();
    return aggregate(mapRed);
  }

  public List getResult(SortedMap sortedMap) throws Exception {
    SortedMap<Integer, ? extends SortedMap<OSHDBTimestamp, ? extends Number>> groupByResult;
    groupByResult = GroupByUtility.nest(sortedMap);
    List<GroupByResult> resultSet = new ArrayList<>();
    Object groupByName;
    Object[] boundaryIds = spatialUtility.getBoundaryIds();
    int count = 0;
    ArrayList<Geometry> boundaries =
        new ArrayList<>(inputProcessor.getProcessingData().getBoundaryList());
    for (Entry<Integer, ? extends SortedMap<OSHDBTimestamp, ? extends Number>> entry : groupByResult
        .entrySet()) {
      List<Result> results = resultUtility.fillElementsResult(entry.getValue(),
          boundaries.get(count), inputProcessor);
      groupByName = boundaryIds[count];
      resultSet.add(new GroupByResult(groupByName, results));
      count++;
    }
    return resultSet;
  }

  public <P extends Geometry & Polygonal> MapAggregator aggregate(
      MapReducer<OSMEntitySnapshot> mapRed) throws Exception {
    List<Geometry> boundaries = new ArrayList<>(geometryFrom.getGeometryList());
    @SuppressWarnings("unchecked") // intentionally as check for P on Polygonal is already performed
    Map<Integer, P> geometries = IntStream.range(0, boundaries.size()).boxed()
        .collect(Collectors.toMap(idx -> idx, idx -> (P) boundaries.get(idx)));
    MapAggregator<OSHDBCombinedIndex<OSHDBTimestamp, Integer>, OSMEntitySnapshot> mapAgg =
        mapRed.aggregateByTimestamp().aggregateByGeometry(geometries);
    if (inputProcessor.getProcessingData().isContainingSimpleFeatureTypes()) {
      mapAgg = inputProcessor.filterOnSimpleFeatures(mapAgg);
    }
    Optional<FilterExpression> filter = inputProcessor.getProcessingData().getFilterExpression();
    if (filter.isPresent()) {
      mapAgg = mapAgg.filter(filter.get());
    }
    return mapAgg;
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
    return Description.aggregate(inputProcessor.isDensity(),
        this.getDescription(), this.getUnit());
  }
}
