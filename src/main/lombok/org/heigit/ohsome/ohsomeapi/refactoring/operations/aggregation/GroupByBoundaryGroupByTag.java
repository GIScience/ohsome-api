package org.heigit.ohsome.ohsomeapi.refactoring.operations.aggregation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.lang3.tuple.Pair;
import org.heigit.ohsome.ohsomeapi.Application;
import org.heigit.ohsome.ohsomeapi.executor.ExecutionUtils;
import org.heigit.ohsome.ohsomeapi.output.Description;
import org.heigit.ohsome.ohsomeapi.output.Metadata;
import org.heigit.ohsome.ohsomeapi.output.Response;
import org.heigit.ohsome.ohsomeapi.output.elements.ElementsResult;
import org.heigit.ohsome.ohsomeapi.output.groupby.GroupByResponse;
import org.heigit.ohsome.ohsomeapi.output.groupby.GroupByResult;
import org.heigit.ohsome.ohsomeapi.refactoring.operations.Operation;
import org.heigit.ohsome.ohsomeapi.refactoring.operations.SnapshotView;
import org.heigit.ohsome.ohsomeapi.utils.GroupByBoundaryGeoJsonGenerator;
import org.heigit.ohsome.oshdb.api.generic.OSHDBCombinedIndex;
import org.heigit.ohsome.oshdb.api.mapreducer.MapAggregator;
import org.heigit.ohsome.oshdb.api.mapreducer.MapReducer;
import org.heigit.ohsome.oshdb.filter.FilterExpression;
import org.heigit.ohsome.oshdb.util.mappable.OSMEntitySnapshot;
import org.locationtech.jts.geom.Geometry;
import org.springframework.beans.factory.annotation.Autowired;

public class GroupByBoundaryGroupByTag extends Group implements Operation, SnapshotView {

  @Autowired
  SnapshotView snapshotView;

  @Override
  public Object compute() throws Exception {
    int keysInt = this.getOSHDBTagKey();
    Integer[] valuesInt = this.getOSHDBTag();
    List<Pair<Integer, Integer>> zeroFill = this.getListOfKeyValuePair(keysInt, valuesInt);
    MapReducer<OSMEntitySnapshot> mapRed = inputProcessor.processParameters(snapshotView);
    var arrGeoms = new ArrayList<>(processingData.getBoundaryList());
    @SuppressWarnings("unchecked") // intentionally as check for P on Polygonal is already performed
    Map<Integer, P> geoms = IntStream.range(0, arrGeoms.size()).boxed()
        .collect(Collectors.toMap(idx -> idx, idx -> arrGeoms.get(idx)));
    MapAggregator<Integer, OSMEntitySnapshot> mapAgg = mapRed.aggregateByGeometry(geoms);
    if (processingData.isContainingSimpleFeatureTypes()) {
      mapAgg = inputProcessor.filterOnSimpleFeatures(mapAgg);
    }
    Optional<FilterExpression> filter = processingData.getFilterExpression();
    if (filter.isPresent()) {
      mapAgg = mapAgg.filter(filter.get());
    }
    var result = ExecutionUtils.computeNestedResult(operation,
        mapAgg.map(f -> ExecutionUtils.mapSnapshotToTags(keysInt, valuesInt, f))
            .aggregateBy(Pair::getKey, zeroFill).map(Pair::getValue)
            .aggregateByTimestamp(OSMEntitySnapshot::getTimestamp));
    var groupByResult = OSHDBCombinedIndex.nest(result);
    GroupByResult[] resultSet = new GroupByResult[groupByResult.entrySet().size()];
    //InputProcessingUtils utils = inputProcessor.getUtils();
    Object[] boundaryIds = utils.getBoundaryIds();
    int count = 0;
    ArrayList<Geometry> boundaries = new ArrayList<>(processingData.getBoundaryList());
    for (var entry : groupByResult.entrySet()) {
      int boundaryIdentifier = entry.getKey().getFirstIndex();
      ElementsResult[] results = ExecutionUtils.fillElementsResult(
          entry.getValue(), inputProcessor.isDensity(), df, boundaries.get(boundaryIdentifier));
      int tagValue = entry.getKey().getSecondIndex().getValue();
      String tagIdentifier;
      // check for non-remainder objects (which do have the defined key and value)
      if (entry.getKey().getSecondIndex().getKey() != -1 && tagValue != -1) {
        tagIdentifier = tt.getOSMTagOf(keysInt, tagValue).toString();
      } else {
        tagIdentifier = "remainder";
      }
      resultSet[count] =
          new GroupByResult(new Object[] {boundaryIds[boundaryIdentifier], tagIdentifier}, results);
      count++;
    }
    // used to remove null objects from the resultSet
    resultSet = Arrays.stream(resultSet).filter(Objects::nonNull).toArray(GroupByResult[]::new);
    Metadata metadata = null;
    if (processingData.isShowMetadata()) {
      long duration = System.currentTimeMillis() - startTime;
      metadata = new Metadata(duration,
          Description.aggregateGroupByBoundaryGroupByTag(inputProcessor.isDensity(),
              operation.getDescription(), operation.getUnit()),
          inputProcessor.getRequestUrlIfGetRequest());
    }
    if ("csv".equalsIgnoreCase(servletRequest.getParameter("format"))) {
      ExecutionUtils exeUtils = new ExecutionUtils(processingData);
      exeUtils.writeCsvResponse(resultSet, servletResponse,
          ExecutionUtils.createCsvTopComments(attribution.getUrl(), attribution.getText(), Application.API_VERSION, metadata));
      return null;
    } else if ("geojson".equalsIgnoreCase(servletRequest.getParameter("format"))) {
      return GroupByResponse.of(attribution, Application.API_VERSION, metadata,
          "FeatureCollection", GroupByBoundaryGeoJsonGenerator.createGeoJsonFeatures(resultSet,
              processingData.getGeoJsonGeoms()));
    }
    return new GroupByResponse(attribution, Application.API_VERSION, metadata,
        resultSet);
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
    return null;
  }
}
