package org.heigit.ohsome.ohsomeapi.executor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SortedMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.heigit.ohsome.ohsomeapi.Application;
import org.heigit.ohsome.ohsomeapi.exception.BadRequestException;
import org.heigit.ohsome.ohsomeapi.inputprocessing.InputProcessor;
import org.heigit.ohsome.ohsomeapi.inputprocessing.ProcessingData;
import org.heigit.ohsome.ohsomeapi.output.Attribution;
import org.heigit.ohsome.ohsomeapi.output.DefaultAggregationResponse;
import org.heigit.ohsome.ohsomeapi.output.Description;
import org.heigit.ohsome.ohsomeapi.output.Metadata;
import org.heigit.ohsome.ohsomeapi.output.Response;
import org.heigit.ohsome.ohsomeapi.output.contributions.ContributionsResult;
import org.heigit.ohsome.ohsomeapi.output.groupby.GroupByResponse;
import org.heigit.ohsome.ohsomeapi.output.groupby.GroupByResult;
import org.heigit.ohsome.ohsomeapi.utils.GroupByBoundaryGeoJsonGenerator;
import org.heigit.ohsome.oshdb.OSHDBTimestamp;
import org.heigit.ohsome.oshdb.api.generic.OSHDBCombinedIndex;
import org.heigit.ohsome.oshdb.api.mapreducer.MapReducer;
import org.heigit.ohsome.oshdb.api.mapreducer.Mappable;
import org.heigit.ohsome.oshdb.filter.FilterExpression;
import org.heigit.ohsome.oshdb.util.celliterator.ContributionType;
import org.heigit.ohsome.oshdb.util.function.SerializablePredicate;
import org.heigit.ohsome.oshdb.util.mappable.OSMContribution;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Polygonal;

/**
 * Includes the execute method for requests mapped to /contributions/count,
 * /contributions/count/density, /contributions/latest/count, /contributions/latest/count/density
 * and /users/count.
 */
public class ContributionsExecutor extends RequestExecutor {
  private static final String CONTRIBUTION_TYPE_PARAMETER = "contributionType";

  private final InputProcessor inputProcessor;
  private final ProcessingData processingData;
  private final long startTime = System.currentTimeMillis();

  /**
   * Initializes a newly created <code>ContributionsExecutor</code> object.
   *
   * @param isDensity the boolean value relative to the density resource
   * @param servletRequest <code>HttpServletRequest</code> of the incoming request
   * @param servletResponse <code>HttpServletResponse</code> of the outgoing response
   */
  public ContributionsExecutor(HttpServletRequest servletRequest,
      HttpServletResponse servletResponse, boolean isDensity) {
    super(servletRequest, servletResponse);
    inputProcessor = new InputProcessor(servletRequest, false, isDensity);
    processingData = inputProcessor.getProcessingData();
  }

  /**
   * Handler method for count calculation of the endpoints /contributions/count,
   * /contributions/density, /contribution/latest/count, /contributions/latest/count/density
   * or /users/count.
   *
   * @param isUsersRequest the boolean value relative to the endpoint /users/count
   * @param isContributionsLatestCount the boolean value relative to the endpoint
   *        /contributions/latest
   * @return DefaultAggregationResponse {@link org.heigit.ohsome.ohsomeapi.output.Response Response}
   * @throws Exception thrown by
   *         {@link org.heigit.ohsome.ohsomeapi.inputprocessing.InputProcessor#processParameters()
   *         processParameters},
   *         {@link org.heigit.ohsome.ohsomeapi.inputprocessing.InputProcessor
   *         #processParameters(ComputeMode) processParameters} and
   *         {@link org.heigit.ohsome.oshdb.api.mapreducer.MapAggregator#count() count}
   * @throws UnsupportedOperationException thrown by
   *         {@link org.heigit.ohsome.ohsomeapi.executor.ContributionsExecutor
   *         #usersCount(MapReducer) usersCount} and
   *         {@link org.heigit.ohsome.ohsomeapi.executor.ContributionsExecutor
   *         #contributionsCount(MapReducer, boolean) contributionsCount}
   */
  public Response count(boolean isUsersRequest, boolean isContributionsLatestCount)
      throws UnsupportedOperationException, Exception {
    MapReducer<OSMContribution> mapRed;
    final SortedMap<OSHDBTimestamp, ? extends Number> result;
    if (isContributionsLatestCount) {
      // the setFullHistory flag needs to be set, because
      // otherwise the MapReducer would be filtered in the Inputprocessor
      // preventing the call of groupByEntity() in contributionsCount()
      inputProcessor.getProcessingData().setFullHistory(true);
    }
    mapRed = inputProcessor.processParameters();
    if (isUsersRequest) {
      result = usersCount(mapRed);
    } else {
      result = contributionsCount(mapRed, isContributionsLatestCount);
    }
    Geometry geom = inputProcessor.getGeometry();
    RequestParameters requestParameters = processingData.getRequestParameters();
    ContributionsResult[] results = ExecutionUtils.fillContributionsResult(result,
        requestParameters.isDensity(), inputProcessor, df, geom);
    Metadata metadata = null;
    if (processingData.isShowMetadata()) {
      long duration = System.currentTimeMillis() - startTime;
      String description;
      if (isUsersRequest) {
        description = Description.countUsers(requestParameters.isDensity());
      } else {
        description = Description.countContributions(requestParameters.isDensity());
      }
      metadata = new Metadata(duration, description,
          inputProcessor.getRequestUrlIfGetRequest(servletRequest));
    }
    if ("csv".equalsIgnoreCase(requestParameters.getFormat())) {
      var exeUtils = new ExecutionUtils(processingData);
      exeUtils.writeCsvResponse(results, servletResponse,
          ExecutionUtils.createCsvTopComments(URL, TEXT, Application.API_VERSION, metadata));
      return null;
    }
    return DefaultAggregationResponse.of(new Attribution(URL, TEXT), Application.API_VERSION,
        metadata, results);
  }

  /**
   * Performs a count calculation for /users/count.
   *
   * @param mapRed a MapReducer of OSM contributions
   * @return SortedMap with counts of users aggregated by timestamp
   * @throws Exception thrown by
   *         {@link org.heigit.ohsome.oshdb.api.mapreducer.MapAggregator #countUniq() countUniq}
   * @throws UnsupportedOperationException thrown by
   *         {@link org.heigit.ohsome.oshdb.api.mapreducer.MapReducer#aggregateByTimestamp()
   *         aggregateByTimeStamp}
   */
  private SortedMap<OSHDBTimestamp, Integer> usersCount(MapReducer<OSMContribution> mapRed)
      throws UnsupportedOperationException, Exception {
    return mapRed.aggregateByTimestamp().map(OSMContribution::getContributorUserId).countUniq();
  }

  /**
   * Performs a count calculation for /contributions/count, /contributions/density,
   * /contribution/latest/count or /contributions/latest/count/density.
   *
   * @param mapRed a MapReducer of OSM contributions
   * @param isContributionsLatest the boolean value relative to the endpoint /contributions/latest
   * @return SortedMap with counts of contributions aggregated by timestamp
   * @throws Exception thrown by
   *         {@link org.heigit.ohsome.oshdb.api.mapreducer.MapAggregator #count() count}
   * @throws UnsupportedOperationException thrown by
   *         {@link org.heigit.ohsome.oshdb.api.mapreducer.MapReducer#aggregateByTimestamp()
   *         aggregateByTimeStamp}
   */
  private SortedMap<OSHDBTimestamp, Integer> contributionsCount(MapReducer<OSMContribution> mapRed,
      boolean isContributionsLatest) throws UnsupportedOperationException, Exception {
    if (isContributionsLatest) {
      MapReducer<List<OSMContribution>> mapRedGroupByEntity = mapRed.groupByEntity();
      Optional<FilterExpression> filter = processingData.getFilterExpression();
      if (filter.isPresent()) {
        mapRedGroupByEntity = mapRedGroupByEntity.filter(filter.get());
      }
      return mapRedGroupByEntity
          .map(contributions -> contributions.get(contributions.size() - 1))
          .filter(contributionsFilter(servletRequest.getParameter(CONTRIBUTION_TYPE_PARAMETER)))
          .aggregateByTimestamp(OSMContribution::getTimestamp)
          .count();
    } else {
      return mapRed
          .filter(contributionsFilter(servletRequest.getParameter(CONTRIBUTION_TYPE_PARAMETER)))
          .aggregateByTimestamp()
          .count();
    }
  }

  /**
   * Handler method for count calculation of the endpoints
   * /contributions/count/[density/]groupBy/boundary and /users/count/[density/]groupBy/boundary.
   *
   * @return GroupByResponse {@link org.heigit.ohsome.ohsomeapi.output.Response Response}
   * @throws Exception thrown by
   *         {@link org.heigit.ohsome.ohsomeapi.inputprocessing.InputProcessor#processParameters()
   *         processParameters},
   *         {@link org.heigit.ohsome.ohsomeapi.inputprocessing.InputProcessor
   *         #processParameters(ComputeMode) processParameters} and
   *         {@link org.heigit.ohsome.oshdb.api.mapreducer.MapAggregator#count() count}
   * @throws UnsupportedOperationException thrown by
   *         {@link org.heigit.ohsome.ohsomeapi.executor.ContributionsExecutor
   *         #usersCount(MapReducer) usersCount} and
   *         {@link org.heigit.ohsome.ohsomeapi.executor.ContributionsExecutor
   *         #contributionsCount(MapReducer, boolean) contributionsCount}
   */
  public <P extends Geometry & Polygonal, V extends Comparable<V> & Serializable> Response
      countGroupByBoundary(boolean isUsersRequest)
      throws UnsupportedOperationException, Exception {
    inputProcessor.getProcessingData().setGroupByBoundary(true);
    var mapRed = inputProcessor.processParameters();
    final var requestParameters = processingData.getRequestParameters();
    List<Geometry> arrGeoms = processingData.getBoundaryList();
    var arrGeomIds = inputProcessor.getUtils().getBoundaryIds();
    @SuppressWarnings("unchecked")
    // intentionally "unchecked" as check for P on Polygonal is already performed, and type of
    // geomIds are either Strings or Integers which are both comparable and serializable
    Map<V, P> geoms = IntStream.range(0, arrGeoms.size()).boxed()
        .collect(Collectors.toMap(idx -> (V) arrGeomIds[idx], idx -> (P) arrGeoms.get(idx)));

    var mapAgg = mapRed
        .aggregateByTimestamp()
        .aggregateByGeometry(geoms)
        .map(OSMContribution.class::cast);

    var filter = inputProcessor.getProcessingData().getFilterExpression();
    if (filter.isPresent()) {
      mapAgg = mapAgg.filter(filter.get());
    }
    SortedMap<OSHDBCombinedIndex<OSHDBTimestamp, V>, Integer> result;
    if (isUsersRequest) {
      result = mapAgg.map(OSMContribution::getContributorUserId).countUniq();
    } else {
      result = mapAgg
          .filter(contributionsFilter(servletRequest.getParameter(CONTRIBUTION_TYPE_PARAMETER)))
          .count();
    }
    var groupByResult = ExecutionUtils.nest(result);
    var resultSet = groupByResult.entrySet().stream().map(entry ->
        new GroupByResult(entry.getKey(), ExecutionUtils.fillContributionsResult(entry.getValue(),
            requestParameters.isDensity(), inputProcessor, df, geoms.get(entry.getKey())
    ))).toArray(GroupByResult[]::new);
    Metadata metadata = null;
    if (processingData.isShowMetadata()) {
      long duration = System.currentTimeMillis() - startTime;
      String description;
      if (isUsersRequest) {
        description = Description.countUsersGroupByBoundary(requestParameters.isDensity());
      } else {
        description = Description.countContributionsGroupByBoundary(requestParameters.isDensity());
      }
      metadata = new Metadata(duration, description,
          inputProcessor.getRequestUrlIfGetRequest(servletRequest));
    }
    if ("geojson".equalsIgnoreCase(requestParameters.getFormat())) {
      return GroupByResponse.of(new Attribution(URL, TEXT), Application.API_VERSION, metadata,
          "FeatureCollection", GroupByBoundaryGeoJsonGenerator.createGeoJsonFeatures(resultSet,
              processingData.getGeoJsonGeoms()));
    } else if ("csv".equalsIgnoreCase(requestParameters.getFormat())) {
      var exeUtils = new ExecutionUtils(processingData);
      exeUtils.writeCsvResponse(resultSet, servletResponse,
          ExecutionUtils.createCsvTopComments(URL, TEXT, Application.API_VERSION, metadata));
      return null;
    }
    return new GroupByResponse(new Attribution(URL, TEXT), Application.API_VERSION, metadata,
        resultSet);
  }

  /**
   * Returns a function to filter contributions by contribution type.
   *
   * @param types the parameter string containing the to-be-filtered contribution types
   * @return a lambda method implementing the filter which can be passed to
   *         {@link Mappable#filter(SerializablePredicate)}
   */
  static SerializablePredicate<OSMContribution> contributionsFilter(String types) {
    if (types == null) {
      return ignored -> true;
    }
    types = types.toUpperCase();
    List<ContributionType> contributionTypes = new ArrayList<>();
    for (String givenType : types.split(",")) {
      switch (givenType) {
        case "CREATION":
          contributionTypes.add(ContributionType.CREATION);
          break;
        case "DELETION":
          contributionTypes.add(ContributionType.DELETION);
          break;
        case "TAGCHANGE":
          contributionTypes.add(ContributionType.TAG_CHANGE);
          break;
        case "GEOMETRYCHANGE":
          contributionTypes.add(ContributionType.GEOMETRY_CHANGE);
          break;
        default:
          throw new BadRequestException("The contribution type must be 'creation', 'deletion',"
              + "'geometryChange', 'tagChange' or a combination of them");
      }
    }
    return contr -> contributionTypes.stream().anyMatch(contr::is);
  }
}
