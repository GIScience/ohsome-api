package org.heigit.ohsome.ohsomeapi.executor;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Supplier;
import org.heigit.bigspatialdata.oshdb.api.object.OSMContribution;
import org.heigit.bigspatialdata.oshdb.api.object.OSMEntitySnapshot;
import org.heigit.bigspatialdata.oshdb.osm.OSMEntity;
import org.heigit.bigspatialdata.oshdb.util.celliterator.ContributionType;
import org.heigit.bigspatialdata.oshdb.util.time.TimestampFormatter;
import org.heigit.ohsome.ohsomeapi.controller.elements.features.ElementsGeometry;
import org.heigit.ohsome.ohsomeapi.inputprocessing.InputProcessingUtils;
import org.heigit.ohsome.ohsomeapi.inputprocessing.SimpleFeatureType;
import org.locationtech.jts.geom.Geometry;
import org.wololo.geojson.Feature;

public class DataExtractionTransformer implements Serializable {

  private final boolean isContributionsLatestEndpoint;
  private final boolean isContributionsEndpoint;
  private final ExecutionUtils exeUtils;
  private final boolean clipGeometries;
  private final String startTimestamp;
  private final InputProcessingUtils utils;
  private final Set<SimpleFeatureType> simpleFeatureTypes;
  private final Set<Integer> keysInt;
  private final boolean includeTags;
  private final boolean includeOSMMetadata;
  private final ElementsGeometry elementsGeometry;
  private final String endTimestamp;
  private final boolean isContainingSimpleFeatureTypes;

  public DataExtractionTransformer(boolean isContributionsLatestEndpoint,
      boolean isContributionsEndpoint, ExecutionUtils exeUtils,
      boolean clipGeometries, String startTimestamp, InputProcessingUtils utils,
      Set<SimpleFeatureType> simpleFeatureTypes, Set<Integer> keysInt, boolean includeTags,
      boolean includeOSMMetadata, ElementsGeometry elementsGeometry, String endTimestamp,
      boolean isContainingSimpleFeatureTypes) {
    this.isContributionsLatestEndpoint = isContributionsLatestEndpoint;
    this.isContributionsEndpoint = isContributionsEndpoint;
    this.exeUtils = exeUtils;
    this.clipGeometries = clipGeometries;
    this.startTimestamp = startTimestamp;
    this.utils = utils;
    this.simpleFeatureTypes = simpleFeatureTypes;
    this.keysInt = keysInt;
    this.includeTags = includeTags;
    this.includeOSMMetadata = includeOSMMetadata;
    this.elementsGeometry = elementsGeometry;
    this.endTimestamp = endTimestamp;
    this.isContainingSimpleFeatureTypes = isContainingSimpleFeatureTypes;
  }

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
          final Geometry geomToCheck = currentGeom;
          boolean addToOutput = addEntityToOutput(isContainingSimpleFeatureTypes, utils,
              simpleFeatureTypes, () -> geomToCheck);
          if (addToOutput) {
            properties = new TreeMap<>();
            if (!isContributionsEndpoint) {
              properties.put("@validFrom", validFrom);
              properties.put("@validTo", validTo);
            } else {
              properties.put("@timestamp", validTo);
            }
            output.add(exeUtils.createOSMFeature(currentEntity, currentGeom, properties, keysInt,
                includeTags, includeOSMMetadata, isContributionsEndpoint, elementsGeometry,
                contribution.getContributionTypes()));
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
        final Geometry geomToCheck = currentGeom;
        boolean addToOutput = addEntityToOutput(isContainingSimpleFeatureTypes, utils,
            simpleFeatureTypes, () -> geomToCheck);
        if (addToOutput) {
          output.add(exeUtils.createOSMFeature(currentEntity, currentGeom, properties, keysInt,
              includeTags, includeOSMMetadata, isContributionsEndpoint, elementsGeometry,
              lastContribution.getContributionTypes()));
        }
      }
    } else if (isContributionsEndpoint) {
      // adds the deletion feature for a /contributions request
      currentGeom = exeUtils.getGeometry(lastContribution, clipGeometries, true);
      properties = new TreeMap<>();
      properties.put("@timestamp",
          TimestampFormatter.getInstance().isoDateTime(lastContribution.getTimestamp()));
      output.add(exeUtils.createOSMFeature(currentEntity, currentGeom, properties, keysInt, false,
          includeOSMMetadata, isContributionsEndpoint, elementsGeometry,
          lastContribution.getContributionTypes()));
    }
    return output;
  }

  public List<Feature> buildUnchangedFeatures(OSMEntitySnapshot snapshot) {
    Map<String, Object> properties = new TreeMap<>();
    OSMEntity entity = snapshot.getEntity();
    if (includeOSMMetadata) {
      properties.put("@lastEdit", entity.getTimestamp().toString());
    }
    Supplier<Geometry> geom;
    if (clipGeometries) {
      geom = snapshot::getGeometry;
    } else {
      geom = snapshot::getGeometryUnclipped;
    }
    properties.put("@snapshotTimestamp",
        TimestampFormatter.getInstance().isoDateTime(snapshot.getTimestamp()));
    properties.put("@validFrom", startTimestamp);
    properties.put("@validTo", endTimestamp);
    boolean addToOutput = addEntityToOutput(isContainingSimpleFeatureTypes, utils,
        simpleFeatureTypes, geom);
    if (addToOutput) {
      return Collections.singletonList(
          exeUtils.createOSMFeature(entity, geom.get(), properties, keysInt, includeTags,
              includeOSMMetadata, isContributionsEndpoint, elementsGeometry, null));
    } else {
      return Collections.emptyList();
    }
  }

  /** Checks whether the given entity should be added to the output (true) or not (false). */
  public static boolean addEntityToOutput(
      boolean isContainingSimpleFeatureTypes,
      InputProcessingUtils utils,
      final Set<SimpleFeatureType> simpleFeatureTypes,
      Supplier<Geometry> currentGeom) {
    boolean addToOutput;
    if (isContainingSimpleFeatureTypes) {
      addToOutput = utils.checkGeometryOnSimpleFeatures(currentGeom.get(), simpleFeatureTypes);
    } else {
      addToOutput = true;
    }
    return addToOutput;
  }
}
