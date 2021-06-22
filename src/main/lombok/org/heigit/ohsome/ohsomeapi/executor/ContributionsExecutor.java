package org.heigit.ohsome.ohsomeapi.executor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.SortedMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.heigit.bigspatialdata.oshdb.api.mapreducer.MapReducer;
import org.heigit.bigspatialdata.oshdb.api.object.OSMContribution;
import org.heigit.bigspatialdata.oshdb.util.OSHDBTimestamp;
import org.heigit.bigspatialdata.oshdb.util.celliterator.ContributionType;
import org.heigit.ohsome.filter.FilterExpression;
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
import org.locationtech.jts.geom.Geometry;

/**
 * Includes the execute method for requests mapped to /contributions/count,
 * /contributions/count/density, /contributions/latest/count, /contributions/latest/count/density
 * and /users/count.
 */
public class ContributionsExecutor extends RequestExecutor {

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
   *         {@link org.heigit.bigspatialdata.oshdb.api.mapreducer.MapAggregator#count() count}
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
   *         {@link org.heigit.bigspatialdata.oshdb.api.mapreducer.MapAggregator #countUniq()
   *         countUniq}
   * @throws UnsupportedOperationException thrown by
   *         {@link org.heigit.bigspatialdata.oshdb.api.mapreducer.MapReducer#aggregateByTimestamp()
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
   *         {@link org.heigit.bigspatialdata.oshdb.api.mapreducer.MapAggregator #count() count}
   * @throws UnsupportedOperationException thrown by
   *         {@link org.heigit.bigspatialdata.oshdb.api.mapreducer.MapReducer#aggregateByTimestamp()
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
      return contributionsFilter(mapRedGroupByEntity
          .map(listContrsPerEntity -> listContrsPerEntity.get(listContrsPerEntity.size() - 1)))
          .aggregateByTimestamp(OSMContribution::getTimestamp).count();
    } else {
      return contributionsFilter(mapRed).aggregateByTimestamp().count();
    }
  }

  /**
   * Filters contributions by contribution type.
   *
   * @param mapRed a MapReducer to be filtered
   * @return MapReducer filtered by contribution type
   */
  private MapReducer<OSMContribution> contributionsFilter(MapReducer<OSMContribution> mapRed) {
    String types = servletRequest.getParameter("contributionType");
    if (types == null) {
      return mapRed;
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
    return mapRed.filter(contr -> contributionTypes.stream().anyMatch(contr::is));
  }
}
