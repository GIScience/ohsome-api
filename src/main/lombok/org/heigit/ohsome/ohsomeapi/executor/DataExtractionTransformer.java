package org.heigit.ohsome.ohsomeapi.executor;

import java.io.Serializable;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.heigit.ohsome.ohsomeapi.controller.dataextraction.elements.ElementsGeometry;
import org.heigit.ohsome.ohsomeapi.inputprocessing.InputProcessingUtils;
import org.heigit.ohsome.ohsomeapi.inputprocessing.SimpleFeatureType;
import org.heigit.ohsome.ohsomeapi.utils.TimestampFormatter;
import org.heigit.ohsome.oshdb.filter.FilterExpression;
import org.heigit.ohsome.oshdb.osm.OSMEntity;
import org.heigit.ohsome.oshdb.util.celliterator.ContributionType;
import org.heigit.ohsome.oshdb.util.mappable.OSMContribution;
import org.heigit.ohsome.oshdb.util.mappable.OSMEntitySnapshot;
import org.locationtech.jts.geom.Geometry;
import org.wololo.geojson.Feature;

/**
 * Used by data extraction requests to create GeoJSON features from OSM entities.
 */
public class DataExtractionTransformer implements Serializable {
  private static final String VALID_TO_PROPERTY = "@validTo";
  private static final String VALID_FROM_PROPERTY = "@validFrom";
  private static final String TIMESTAMP_PROPERTY = "@timestamp";
  private static final String CONTRIBUTION_CHANGESET_ID_PROPERTY = "@contributionChangesetId";

  private final String startTimestamp;
  private final String endTimestamp;
  private final FilterExpression filter;
  private final boolean isContributionsEndpoint;
  private final boolean isContributionsLatestEndpoint;
  private final boolean clipGeometries;
  private final boolean includeTags;
  private final boolean includeOSMMetadata;
  private final boolean includeContributionTypes;
  private final ElementsGeometry outputGeometry;
  private final InputProcessingUtils inputUtils;
  private final ExecutionUtils exeUtils;
  @Deprecated
  private final Set<Integer> keysInt;
  @Deprecated
  private final Set<SimpleFeatureType> simpleFeatureTypes;
  @Deprecated
  private final boolean isContainingSimpleFeatureTypes;

  /**
   * Creates a new data extraction transformer, adhering to the given parameters.
   *
   * @param startTimestamp start timestamp of the query
   * @param endTimestamp end timestamp of the query
   * @param filter the filter of the query
   * @param isContributionsEndpoint set true if the requested resource is a
*           `/contributions` endpoint
   * @param isContributionsLatestEndpoint set true if the requested resource is a
*           `/contributions/latest` endpoint
   * @param clipGeometries whether or not the output geometries should be clipped to the query
   *        area-of-interest or not
   * @param includeTags set true if the result should include all OSM entity's tags as GeoJSON
   *        feature properties
   * @param includeOSMMetadata set true if the result should include the OSM entity metadata (e.g.
   *        changeset id, timestamp, version number)
   * @param includeContributionTypes set true if the result should include the contribution type
   *        for `/elements/contributions` resources
   * @param inputUtils input processing utility object
   * @param exeUtils the execution utils object
   * @param keysInt (for the deprecated `keys` filter parameter) set the list of always to be
   *        returned OSM tags in the GeoJSON's features' properties
   * @param outputGeometry specifies what should be returned as the GeoJSON feature's geometry:
   *        either the full geometry, its bbox or its centroid.
   * @param simpleFeatureTypes if the query uses the (deprecated) types parameter, and it contains
   *        simple feature "geometry" types, specify the set of to be returned geometry types here
   * @param isContainingSimpleFeatureTypes set true if the query uses the (deprecated) types
   */
  public DataExtractionTransformer(String startTimestamp, String endTimestamp,
      FilterExpression filter, boolean isContributionsEndpoint,
      boolean isContributionsLatestEndpoint, boolean clipGeometries, boolean includeTags,
      boolean includeOSMMetadata, boolean includeContributionTypes, InputProcessingUtils inputUtils,
      ExecutionUtils exeUtils, Set<Integer> keysInt, ElementsGeometry outputGeometry,
      Set<SimpleFeatureType> simpleFeatureTypes, boolean isContainingSimpleFeatureTypes) {
    this.isContributionsLatestEndpoint = isContributionsLatestEndpoint;
    this.isContributionsEndpoint = isContributionsEndpoint;
    this.exeUtils = exeUtils;
    this.clipGeometries = clipGeometries;
    this.startTimestamp = startTimestamp;
    this.inputUtils = inputUtils;
    this.simpleFeatureTypes = simpleFeatureTypes;
    this.filter = filter;
    this.keysInt = keysInt;
    this.includeTags = includeTags;
    this.includeOSMMetadata = includeOSMMetadata;
    this.includeContributionTypes = includeContributionTypes;
    this.outputGeometry = outputGeometry;
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
   * @param contributions list of modifications of a single OSMEntity.
   * @return list of GeoJSON features corresponding to the given OSM entity's modifications.
   */
  public List<Feature> buildChangedFeatures(List<OSMContribution> contributions) {
    if (isContributionsEndpoint) {
      return buildChangedFeaturesContributions(contributions);
    } else {
      return buildChangedFeaturesFullHistory(contributions);
    }
  }

  private List<Feature> buildChangedFeaturesContributions(List<OSMContribution> contributions) {
    List<Feature> output = new LinkedList<>();

    for (int i = 0; i < contributions.size(); i++) {
      if (isContributionsLatestEndpoint && i < contributions.size() - 1) {
        // skip to end the loop when using contributions/latest endpoint
        continue;
      }
      var contribution = contributions.get(i);
      var currentEntity = contribution.getEntityAfter();
      var currentGeom = ExecutionUtils.getGeometry(contribution, clipGeometries, false);
      var timestamp = TimestampFormatter.getInstance().isoDateTime(contribution.getTimestamp());

      Map<String, Object> properties = new TreeMap<>();

      properties.put(TIMESTAMP_PROPERTY, timestamp);
      properties.put(CONTRIBUTION_CHANGESET_ID_PROPERTY, contribution.getChangesetId());
      output.add(exeUtils.createOSMFeature(currentEntity, currentGeom, properties, keysInt,
          includeTags, includeOSMMetadata, includeContributionTypes, isContributionsEndpoint,
          outputGeometry, contribution.getContributionTypes()));
    }

    return output;
  }

  private List<Feature> buildChangedFeaturesFullHistory(List<OSMContribution> contributions) {
    List<Feature> output = new LinkedList<>();
    Map<String, Object> properties;
    Geometry currentGeom = null;
    OSMEntity currentEntity = null;
    String validFrom = null;
    String validTo;
    boolean skipNext = false;

    // first contribution:
    OSMContribution firstContribution = contributions.get(0);
    if (firstContribution.is(ContributionType.CREATION)) {
      skipNext = true;
    } else {
      // if not "creation": take "before" as starting "row" (geom, tags), valid_from = t_start
      currentEntity = firstContribution.getEntityBefore();
      currentGeom = ExecutionUtils.getGeometry(firstContribution, clipGeometries, true);
      validFrom = startTimestamp;
    }

    // then for each contribution:
    for (OSMContribution contribution : contributions) {
      // set valid_to of previous row
      validTo = TimestampFormatter.getInstance().isoDateTime(contribution.getTimestamp());
      if (!skipNext && currentGeom != null && !currentGeom.isEmpty()) {
        boolean addToOutput = addEntityToOutput(currentEntity, currentGeom);
        if (addToOutput) {
          properties = new TreeMap<>();
          properties.put(VALID_FROM_PROPERTY, validFrom);
          properties.put(VALID_TO_PROPERTY, validTo);
          output.add(exeUtils.createOSMFeature(currentEntity, currentGeom, properties, keysInt,
              includeTags, includeOSMMetadata, includeContributionTypes, isContributionsEndpoint,
              outputGeometry, contribution.getContributionTypes()));
        }
      }
      skipNext = false;
      if (contribution.is(ContributionType.DELETION)) {
        // if deletion: skip output of next row
        skipNext = true;
      } else {
        // else: take "after" as next row
        currentEntity = contribution.getEntityAfter();
        currentGeom = ExecutionUtils.getGeometry(contribution, clipGeometries, false);
        validFrom = TimestampFormatter.getInstance().isoDateTime(contribution.getTimestamp());
      }
    }

    // after loop:
    OSMContribution lastContribution = contributions.get(contributions.size() - 1);
    currentGeom = ExecutionUtils.getGeometry(lastContribution, clipGeometries, false);
    currentEntity = lastContribution.getEntityAfter();
    if (!lastContribution.is(ContributionType.DELETION)) {
      // if last contribution was not "deletion": set valid_to = t_end
      validTo = endTimestamp;
      properties = new TreeMap<>();
      properties.put(VALID_FROM_PROPERTY, validFrom);
      properties.put(VALID_TO_PROPERTY, validTo);
      if (!currentGeom.isEmpty()) {
        boolean addToOutput = addEntityToOutput(currentEntity, currentGeom);
        if (addToOutput) {
          output.add(exeUtils.createOSMFeature(currentEntity, currentGeom, properties, keysInt,
              includeTags, includeOSMMetadata, includeContributionTypes, isContributionsEndpoint,
              outputGeometry, lastContribution.getContributionTypes()));
        }
      }
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
   * @param snapshot OSM entity to return.
   * @return Either a singleton of a GeoJSON feature representing this OSM entity, or an
   *         empty collection if it doesn't fit the given (simple feature types) filter.
   */
  public List<Feature> buildUnchangedFeatures(OSMEntitySnapshot snapshot) {
    Map<String, Object> properties = new TreeMap<>();
    Geometry geom;
    if (clipGeometries) {
      geom = snapshot.getGeometry();
    } else {
      geom = snapshot.getGeometryUnclipped();
    }
    properties.put(VALID_FROM_PROPERTY, startTimestamp);
    properties.put(VALID_TO_PROPERTY, endTimestamp);
    OSMEntity entity = snapshot.getEntity();
    boolean addToOutput = addEntityToOutput(entity, geom);
    if (addToOutput) {
      return Collections.singletonList(exeUtils.createOSMFeature(entity, geom, properties,
          keysInt, includeTags, includeOSMMetadata, includeContributionTypes,
          isContributionsEndpoint, outputGeometry, EnumSet.noneOf(ContributionType.class)));
    } else {
      return Collections.emptyList();
    }
  }

  /** Checks whether the given entity should be added to the output (true) or not (false). */
  private boolean addEntityToOutput(OSMEntity currentEntity, Geometry currentGeom) {
    if (isContainingSimpleFeatureTypes) {
      return inputUtils.checkGeometryOnSimpleFeatures(currentGeom, simpleFeatureTypes);
    } else {
      return filter == null || filter.applyOSMGeometry(currentEntity, currentGeom);
    }
  }
}
