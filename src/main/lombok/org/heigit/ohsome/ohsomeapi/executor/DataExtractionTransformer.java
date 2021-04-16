package org.heigit.ohsome.ohsomeapi.executor;

import java.io.Serializable;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.heigit.bigspatialdata.oshdb.api.object.OSMContribution;
import org.heigit.bigspatialdata.oshdb.api.object.OSMEntitySnapshot;
import org.heigit.bigspatialdata.oshdb.osm.OSMEntity;
import org.heigit.bigspatialdata.oshdb.util.celliterator.ContributionType;
import org.heigit.bigspatialdata.oshdb.util.time.TimestampFormatter;
import org.heigit.ohsome.filter.FilterExpression;
import org.heigit.ohsome.ohsomeapi.controller.dataextraction.elements.ElementsGeometry;
import org.heigit.ohsome.ohsomeapi.inputprocessing.InputProcessingUtils;
import org.heigit.ohsome.ohsomeapi.inputprocessing.SimpleFeatureType;
import org.locationtech.jts.geom.Geometry;
import org.wololo.geojson.Feature;

/**
 * Used by data extraction requests to create GeoJSON features from OSM entities.
 */
public class DataExtractionTransformer implements Serializable {

  private final boolean isContributionsLatestEndpoint;
  private final boolean isContributionsEndpoint;
  private final ExecutionUtils exeUtils;
  private final boolean clipGeometries;
  private final String startTimestamp;
  private final InputProcessingUtils utils;
  @Deprecated
  private final Set<SimpleFeatureType> simpleFeatureTypes;
  private final FilterExpression filter;
  @Deprecated
  private final Set<Integer> keysInt;
  private final boolean includeTags;
  private final boolean includeOSMMetadata;
  private final boolean includeContributionTypes;
  private final ElementsGeometry elementsGeometry;
  private final String endTimestamp;
  @Deprecated
  private final boolean isContainingSimpleFeatureTypes;

  /**
   * Creates a new data extraction transformer, adhering to the given parameters.
   *
   * @param isContributionsLatestEndpoint set true if the requested resource is a
   *                                      `/contributions/latest` endpoint
   * @param isContributionsEndpoint set true if the requested resource is a
   *                                `/contributions` endpoint
   * @param exeUtils the execution utils object
   * @param clipGeometries whether or not the output geometries should be clipped to the query
   *                       area-of-interest or not
   * @param startTimestamp start timestamp of the query
   * @param utils input processing utility object
   * @param simpleFeatureTypes if the query uses the (deprecated) types parameter, and it contains
   *                           simple feature "geometry" types, specify the set of to be returned
   *                           geometry types here
   * @param filter the filter of the query
   * @param keysInt (for the deprecated `keys` filter parameter) set the list of always to be
   *                returned OSM tags in the GeoJSON's features' properties
   * @param includeTags set true if the result should include all OSM entity's tags as GeoJSON
   *                    feature properties
   * @param includeOSMMetadata set true if the result should include the OSM entity metadata (e.g.
   *                           cahngeset id, timestamp, version number)
   * @param includeContributionTypes set true if the result should include the contribution type
   *                                 for `/elements/contributions` resources
   * @param elementsGeometry specifies what should be returned as the GeoJSON feature's geometry:
   *                         either the full geometry, its bbox or its centroid.
   * @param endTimestamp end timestamp of the query
   * @param isContainingSimpleFeatureTypes set true if the query uses the (deprecated) types
   *                                       parameter and it contains simple feature "geometry" types
   */
  public DataExtractionTransformer(boolean isContributionsLatestEndpoint,
      boolean isContributionsEndpoint, ExecutionUtils exeUtils, boolean clipGeometries,
      String startTimestamp, InputProcessingUtils utils, Set<SimpleFeatureType> simpleFeatureTypes,
      FilterExpression filter, Set<Integer> keysInt, boolean includeTags,
      boolean includeOSMMetadata, boolean includeContributionTypes,
      ElementsGeometry elementsGeometry, String endTimestamp,
      boolean isContainingSimpleFeatureTypes) {
    this.isContributionsLatestEndpoint = isContributionsLatestEndpoint;
    this.isContributionsEndpoint = isContributionsEndpoint;
    this.exeUtils = exeUtils;
    this.clipGeometries = clipGeometries;
    this.startTimestamp = startTimestamp;
    this.utils = utils;
    this.simpleFeatureTypes = simpleFeatureTypes;
    this.filter = filter;
    this.keysInt = keysInt;
    this.includeTags = includeTags;
    this.includeOSMMetadata = includeOSMMetadata;
    this.includeContributionTypes = includeContributionTypes;
    this.elementsGeometry = elementsGeometry;
    this.endTimestamp = endTimestamp;
    this.isContainingSimpleFeatureTypes = isContainingSimpleFeatureTypes;
  }

  /**
   * Returns a list of GeoJSON features representing the given OSM contributions.
   *
   * <p>
   *   The output is slightly different between the `/contributions` endpoint and the full history
   *   data extraction, but always one GeoJSON feature for each modification of an OSM entity is
   *   returned. Contribution endpoints optionally return the contribution type annotated in the
   *   resulting GeoJSON properties, while full history endpoints include validFrom-validTo dates.
   * </p>
   *
   * @param contributions The list of modifications of a single OSMEntity.
   * @return A list of GeoJSON features corresponding to the given OSM entity's modifications.
   */
  public List<Feature> buildChangedFeatures(List<OSMContribution> contributions) {
    List<Feature> output = new LinkedList<>();
    Map<String, Object> properties;
    Geometry currentGeom = null;
    OSMEntity currentEntity = null;
    String validFrom = null;
    String validTo;
    boolean skipNext = false;
    if (!isContributionsLatestEndpoint) {
      // first contribution:
      OSMContribution firstContribution = contributions.get(0);
      if (firstContribution.is(ContributionType.CREATION) && !isContributionsEndpoint) {
        skipNext = true;
      } else {
        // if not "creation": take "before" as starting "row" (geom, tags), valid_from = t_start
        currentEntity = firstContribution.getEntityBefore();
        currentGeom = exeUtils.getGeometry(firstContribution, clipGeometries, true);
        validFrom = startTimestamp;
      }
      // then for each contribution:
      for (int i = 0; i < contributions.size(); i++) {
        if (i == contributions.size() - 1 && isContributionsEndpoint) {
          // end the loop when last contribution is reached as it gets added later on
          break;
        }
        OSMContribution contribution = contributions.get(i);
        if (isContributionsEndpoint) {
          currentEntity = contribution.getEntityAfter();
          currentGeom = exeUtils.getGeometry(contribution, clipGeometries, false);
          validFrom = TimestampFormatter.getInstance().isoDateTime(contribution.getTimestamp());
        }
        // set valid_to of previous row
        validTo = TimestampFormatter.getInstance().isoDateTime(contribution.getTimestamp());
        if (!skipNext && currentGeom != null && !currentGeom.isEmpty()) {
          boolean addToOutput = addEntityToOutput(currentEntity, currentGeom);
          if (addToOutput) {
            properties = new TreeMap<>();
            if (!isContributionsEndpoint) {
              properties.put("@validFrom", validFrom);
              properties.put("@validTo", validTo);
            } else {
              properties.put("@timestamp", validTo);
            }
            output.add(exeUtils.createOSMFeature(currentEntity, currentGeom, properties, keysInt,
                includeTags, includeOSMMetadata, includeContributionTypes, isContributionsEndpoint,
                elementsGeometry, contribution.getContributionTypes()));
          }
        }
        skipNext = false;
        if (contribution.is(ContributionType.DELETION)) {
          // if deletion: skip output of next row
          skipNext = true;
        } else if (!isContributionsEndpoint) {
          // else: take "after" as next row
          currentEntity = contribution.getEntityAfter();
          currentGeom = exeUtils.getGeometry(contribution, clipGeometries, false);
          validFrom = TimestampFormatter.getInstance().isoDateTime(contribution.getTimestamp());
        }
      }
    }
    // after loop:
    OSMContribution lastContribution = contributions.get(contributions.size() - 1);
    currentGeom = exeUtils.getGeometry(lastContribution, clipGeometries, false);
    currentEntity = lastContribution.getEntityAfter();
    if (!lastContribution.is(ContributionType.DELETION)) {
      // if last contribution was not "deletion": set valid_to = t_end
      validTo = endTimestamp;
      properties = new TreeMap<>();
      if (!isContributionsEndpoint) {
        properties.put("@validFrom", validFrom);
        properties.put("@validTo", validTo);
      } else {
        properties.put("@timestamp",
            TimestampFormatter.getInstance().isoDateTime(lastContribution.getTimestamp()));
      }
      if (!currentGeom.isEmpty()) {
        boolean addToOutput = addEntityToOutput(currentEntity, currentGeom);
        if (addToOutput) {
          output.add(exeUtils.createOSMFeature(currentEntity, currentGeom, properties, keysInt,
              includeTags, includeOSMMetadata, includeContributionTypes, isContributionsEndpoint,
              elementsGeometry, lastContribution.getContributionTypes()));
        }
      }
    } else if (isContributionsEndpoint) {
      // adds the deletion feature for a /contributions request
      currentGeom = exeUtils.getGeometry(lastContribution, clipGeometries, true);
      properties = new TreeMap<>();
      properties.put("@timestamp",
          TimestampFormatter.getInstance().isoDateTime(lastContribution.getTimestamp()));
      output.add(exeUtils.createOSMFeature(currentEntity, currentGeom, properties, keysInt, false,
          includeOSMMetadata, includeContributionTypes, isContributionsEndpoint, elementsGeometry,
          lastContribution.getContributionTypes()));
    }
    return output;
  }

  /**
   * Returns either a singleton of the given OSM entity (snapshot), or an empty list, if it doesn't
   * match the given (simple feature type) filter.
   *
   * <p>
   *   This is only used by full history data extraction requests, to also include entities in the
   *   result which don't change at all during the given start and end times.
   * </p>
   *
   * @param snapshot The osm entity to return.
   * @return Either a singleton of a GeoJSON feature representing this OSM entity, or an
   *         empty collection if it doesn't fit the given (simple feature types) filter.
   */
  public List<Feature> buildUnchangedFeatures(OSMEntitySnapshot snapshot) {
    Map<String, Object> properties = new TreeMap<>();
    OSMEntity entity = snapshot.getEntity();
    if (includeOSMMetadata) {
      properties.put("@lastEdit", entity.getTimestamp().toString());
    }
    Geometry geom;
    if (clipGeometries) {
      geom = snapshot.getGeometry();
    } else {
      geom = snapshot.getGeometryUnclipped();
    }
    properties.put("@snapshotTimestamp",
        TimestampFormatter.getInstance().isoDateTime(snapshot.getTimestamp()));
    properties.put("@validFrom", startTimestamp);
    properties.put("@validTo", endTimestamp);
    boolean addToOutput = addEntityToOutput(entity, geom);
    if (addToOutput) {
      return Collections.singletonList(exeUtils.createOSMFeature(entity, geom, properties,
          keysInt, includeTags, includeOSMMetadata, includeContributionTypes,
          isContributionsEndpoint, elementsGeometry, EnumSet.noneOf(ContributionType.class)));
    } else {
      return Collections.emptyList();
    }
  }

  /** Checks whether the given entity should be added to the output (true) or not (false). */
  private boolean addEntityToOutput(OSMEntity currentEntity, Geometry currentGeom) {
    if (isContainingSimpleFeatureTypes) {
      return utils.checkGeometryOnSimpleFeatures(currentGeom, simpleFeatureTypes);
    } else {
      return filter == null || filter.applyOSMGeometry(currentEntity, currentGeom);
    }
  }
}
