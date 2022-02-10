package org.heigit.ohsome.ohsomeapi.utilities;

import java.util.List;
import org.geojson.Feature;
import org.geojson.GeoJsonObject;
import org.heigit.ohsome.ohsomeapi.output.Result;
import org.heigit.ohsome.ohsomeapi.output.contributions.ContributionsResult;
import org.heigit.ohsome.ohsomeapi.output.elements.ElementsResult;
import org.heigit.ohsome.ohsomeapi.output.groupby.GroupByObject;
import org.heigit.ohsome.ohsomeapi.output.groupby.GroupByResult;
import org.heigit.ohsome.ohsomeapi.output.ratio.RatioGroupByResult;
import org.heigit.ohsome.ohsomeapi.output.ratio.RatioResult;

/**
 * Creates the GeoJson features used in the GeoJson response for the /groupBy/boundary endpoints.
 */
public class GroupByBoundaryGeoJsonGenerator {

  /** Creates the GeoJson features used in the GeoJson response.*/
  public static Feature[] createGeoJsonFeatures(List results,
      GeoJsonObject[] geojsonGeoms) {
    int groupByResultsLength = results.size();
    int groupByResultCount = 0;
    int tstampCount = 0;
    int boundaryCount = 0;
    Feature[] features;
    if (results.get(0) instanceof GroupByResult) {
      features = generateGroupByResultGeoJson(results, geojsonGeoms, groupByResultsLength,
          groupByResultCount, tstampCount, boundaryCount);
    } else {
      features = generateRatioGroupByResultGeoJson(results, geojsonGeoms, groupByResultsLength,
          groupByResultCount, tstampCount);
    }
    return features;
  }

  private static Feature[] generateRatioGroupByResultGeoJson(List<GroupByObject> results,
      GeoJsonObject[] geojsonGeoms, int groupByResultsLength, int groupByResultCount,
      int tstampCount) {
    Feature[] features;
    RatioGroupByResult[] groupByResults = (RatioGroupByResult[]) results.toArray();
    int resultLength = groupByResults[0].getRatioResult().length;
    int featuresLength = groupByResultsLength * resultLength;
    features = new Feature[featuresLength];
    for (int i = 0; i < featuresLength; i++) {
      RatioResult result = groupByResults[groupByResultCount].getRatioResult()[tstampCount];
      String tstamp = result.getTimestamp();
      Feature feature = fillGeojsonFeature(results, groupByResultCount, tstamp);
      feature.setProperty("value", result.getValue());
      feature.setProperty("value2", result.getValue2());
      feature.setProperty("ratio", result.getRatio());
      feature.setGeometry(geojsonGeoms[groupByResultCount]);
      tstampCount++;
      if (tstampCount == resultLength) {
        tstampCount = 0;
        groupByResultCount++;
      }
      features[i] = feature;
    }
    return features;
  }

  /**
   * @throws UnsupportedOperationException if one of the values contained in results is not an
   *         instance of {@link
   *         org.heigit.ohsome.ohsomeapi.output.elements.ElementsResult ElementsResult}, or
   *         {@link org.heigit.ohsome.ohsomeapi.output.contributions.ContributionsResult
   *         ContributionsResult}
   */
  private static Feature[] generateGroupByResultGeoJson(List<GroupByObject> results,
      GeoJsonObject[] geojsonGeoms, int groupByResultsLength, int groupByResultCount,
      int tstampCount, int boundaryCount) {
    Feature[] features;
    GroupByResult[] groupByResults = (GroupByResult[]) results.toArray();
    int resultLength = groupByResults[0].getResult().size();
    int featuresLength = groupByResultsLength * resultLength;
    int nestedGroupByNextBoundaryInterval = featuresLength / geojsonGeoms.length;
    features = new Feature[featuresLength];
    for (int i = 0; i < featuresLength; i++) {
      Result res = (Result) groupByResults[groupByResultCount].getResult().get(tstampCount);
      Feature feature;
      if (res instanceof ElementsResult) {
        ElementsResult result = (ElementsResult) res;
        String tstamp = result.getTimestamp();
        feature = fillGeojsonFeature(results, groupByResultCount, tstamp);
      } else if (res instanceof ContributionsResult) {
        ContributionsResult result = (ContributionsResult) res;
        String tstampFrom = result.getFromTimestamp();
        String tstampTo = result.getToTimestamp();
        feature = fillGeojsonFeature(results, groupByResultCount, tstampFrom, tstampTo);
      } else {
        throw new UnsupportedOperationException();
      }
      feature.setProperty("value", res.getValue());
      // needed for /groupBy/boundary/groupBy/tag
      if (results.get(groupByResultCount).getGroupByObject() instanceof Object[]) {
        feature.setGeometry(geojsonGeoms[boundaryCount]);
        if ((i + 1) % nestedGroupByNextBoundaryInterval == 0) {
          boundaryCount++;
        }
      } else {
        feature.setGeometry(geojsonGeoms[groupByResultCount]);
      }
      tstampCount++;
      if (tstampCount == resultLength) {
        tstampCount = 0;
        groupByResultCount++;
      }
      features[i] = feature;
    }
    return features;
  }

  /** Fills a GeoJSON Feature with the groupByBoundaryId, the timestamp and the geometry. */
  private static Feature fillGeojsonFeature(List<GroupByObject> results, int groupByResultCount,
      String timestamp) {
    Feature feature = makeGeojsonFeature(results, groupByResultCount, timestamp);
    feature.setProperty("timestamp", timestamp);
    return feature;
  }

  /** Fills a GeoJSON Feature with the groupByBoundaryId, the time interval and the geometry. */
  private static Feature fillGeojsonFeature(List<GroupByObject> results, int groupByResultCount,
      String timestampFrom, String timestampTo) {
    Feature feature =
        makeGeojsonFeature(results, groupByResultCount, timestampFrom + "-" + timestampTo);
    feature.setProperty("timestampFrom", timestampFrom);
    feature.setProperty("timestampTo", timestampTo);
    return feature;
  }

  /** Fills a GeoJSON Feature with the groupByBoundaryId and the geometry. */
  private static Feature makeGeojsonFeature(List<GroupByObject> results, int groupByResultCount,
      String id) {
    Object groupByBoundaryId = results.get(groupByResultCount).getGroupByObject();
    Feature feature = new Feature();
    if (groupByBoundaryId instanceof Object[]) {
      Object[] groupByBoundaryIdArr = (Object[]) groupByBoundaryId;
      String boundaryTagId =
          groupByBoundaryIdArr[0].toString() + "_" + groupByBoundaryIdArr[1].toString();
      feature.setId(boundaryTagId + "@" + id);
      feature.setProperty("groupByBoundaryId", boundaryTagId);
    } else {
      feature.setId(groupByBoundaryId + "@" + id);
      feature.setProperty("groupByBoundaryId", groupByBoundaryId);
    }
    return feature;
  }

}
