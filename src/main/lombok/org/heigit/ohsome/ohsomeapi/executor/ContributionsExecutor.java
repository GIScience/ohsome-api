package org.heigit.ohsome.ohsomeapi.executor;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.SortedMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.heigit.bigspatialdata.oshdb.api.db.OSHDBIgnite;
import org.heigit.bigspatialdata.oshdb.api.db.OSHDBIgnite.ComputeMode;
import org.heigit.bigspatialdata.oshdb.api.mapreducer.MapReducer;
import org.heigit.bigspatialdata.oshdb.api.object.OSMContribution;
import org.heigit.bigspatialdata.oshdb.util.OSHDBTimestamp;
import org.heigit.bigspatialdata.oshdb.util.celliterator.ContributionType;
import org.heigit.ohsome.filter.FilterExpression;
import org.heigit.ohsome.ohsomeapi.Application;
import org.heigit.ohsome.ohsomeapi.exception.BadRequestException;
import org.heigit.ohsome.ohsomeapi.inputprocessing.InputProcessor;
import org.heigit.ohsome.ohsomeapi.inputprocessing.ProcessingData;
import org.heigit.ohsome.ohsomeapi.oshdb.DbConnData;
import org.heigit.ohsome.ohsomeapi.output.Attribution;
import org.heigit.ohsome.ohsomeapi.output.DefaultAggregationResponse;
import org.heigit.ohsome.ohsomeapi.output.Description;
import org.heigit.ohsome.ohsomeapi.output.Metadata;
import org.heigit.ohsome.ohsomeapi.output.Response;
import org.heigit.ohsome.ohsomeapi.output.contributions.ContributionsResult;
import org.locationtech.jts.geom.Geometry;

/**
 * Includes the execute method for requests mapped to /contributions/count,
 * /contributions/latest/count and /users/count.
 */
public class ContributionsExecutor extends RequestExecutor {

  private final InputProcessor inputProcessor;
  private final ProcessingData processingData;
  private final long startTime = System.currentTimeMillis();

  public ContributionsExecutor(HttpServletRequest servletRequest,
      HttpServletResponse servletResponse, boolean isDensity) {
    super(servletRequest, servletResponse);
    inputProcessor = new InputProcessor(servletRequest, false, isDensity);
    processingData = inputProcessor.getProcessingData();
  }

  /**
   * Performs a count calculation using contributions for the endpoints /contributions/count,
   * /contribution/latest/count or /users/count.
   *
   * @return {@link org.heigit.ohsome.ohsomeapi.output.Response Response}
   * @throws Exception thrown by
   *         {@link org.heigit.ohsome.ohsomeapi.inputprocessing.InputProcessor#processParameters()
   *         processParameters},
   *         {@link org.heigit.bigspatialdata.oshdb.api.mapreducer.MapAggregator#count() count}
   */
  public Response count(boolean isUsersRequest, boolean isContributionsLatestCount)
      throws Exception {
    MapReducer<OSMContribution> mapRed;
    final SortedMap<OSHDBTimestamp, ? extends Number> result;
    if (isContributionsLatestCount) {
      inputProcessor.getProcessingData().setFullHistory(true);
    }
    if (DbConnData.db instanceof OSHDBIgnite) {
      // on ignite: Use AffinityCall backend, which is the only one properly supporting streaming
      // of result data, without buffering the whole result in memory before returning the result.
      // This allows to write data out to the client via a chunked HTTP response.
      mapRed = inputProcessor.processParameters(ComputeMode.AffinityCall);
    } else {
      mapRed = inputProcessor.processParameters();
    }
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
      ExecutionUtils exeUtils = new ExecutionUtils(processingData);
      exeUtils.writeCsvResponse(results, servletResponse,
          ExecutionUtils.createCsvTopComments(URL, TEXT, Application.API_VERSION, metadata));
      return null;
    }
    return DefaultAggregationResponse.of(new Attribution(URL, TEXT), Application.API_VERSION,
        metadata, results);
  }

  public SortedMap<OSHDBTimestamp, Integer> usersCount(MapReducer<OSMContribution> mapRed)
      throws UnsupportedOperationException, Exception {
    return mapRed.aggregateByTimestamp().map(OSMContribution::getContributorUserId).countUniq();
  }
  
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

  private MapReducer<OSMContribution> contributionsFilter(MapReducer<OSMContribution> mapRed) {
    String contributionType = servletRequest.getParameter("contributionType");
    if (contributionType == null) {
      return mapRed;
    }
    List<String> contrTypes =
        Arrays.asList("CREATION", "DELETION", "GEOMETRYCHANGE", "TAGCHANGE", "OTHERCHANGES");
    contributionType = contributionType.toUpperCase();
    if (!contrTypes.contains(contributionType)) {
      throw new BadRequestException(
          "The contribution type must be 'creation', 'deletion', 'geometryChange', 'tagChange' "
              + "or 'otherChanges'.");
    }
    if (contributionType.equals("OTHERCHANGES")) {
      return mapRed.filter(contr -> contr.getContributionTypes().isEmpty());
    } else {
      var string = new StringBuilder(contributionType);
      if (contributionType.equals("TAGCHANGE") || contributionType.equals("GEOMETRYCHANGE")) {
        string.insert(contributionType.length() - 6, '_');
      }
      return mapRed.filter(contr -> contr.is(ContributionType.valueOf(string.toString())));
    }
  }
}
