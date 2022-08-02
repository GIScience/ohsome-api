package org.heigit.ohsome.ohsomeapi.executor;


import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.EnumSet;
import java.util.Set;
import java.util.function.Predicate;
import org.heigit.ohsome.ohsomeapi.exception.BadRequestException;
import org.heigit.ohsome.oshdb.OSHDBTimestamp;
import org.heigit.ohsome.oshdb.osh.OSHEntity;
import org.heigit.ohsome.oshdb.osm.OSMEntity;
import org.heigit.ohsome.oshdb.util.celliterator.ContributionType;
import org.heigit.ohsome.oshdb.util.mappable.OSMContribution;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Geometry;

/**
 * Test class for the {@link ContributionsExecutor} class.
 */
public class ContributionsExecutorTest {

  @Test
  public void contributionsFilter() {
    Predicate<OSMContribution> filter;
    // check single value
    filter = ExecutionUtils.contributionsFilter("creation");
    assertTrue(filter.test(createOSMContribution(EnumSet.of(ContributionType.CREATION))));
    assertFalse(filter.test(createOSMContribution(EnumSet.of(ContributionType.DELETION))));
    // check multiple values
    filter = ExecutionUtils.contributionsFilter("geometryChange,tagChange");
    assertTrue(filter.test(createOSMContribution(EnumSet.of(ContributionType.GEOMETRY_CHANGE))));
    assertTrue(filter.test(createOSMContribution(EnumSet.of(ContributionType.TAG_CHANGE))));
    assertFalse(filter.test(createOSMContribution(EnumSet.of(ContributionType.DELETION))));
    // check spelling
    filter = ExecutionUtils.contributionsFilter("dElEtIoN");
    assertTrue(filter.test(createOSMContribution(EnumSet.of(ContributionType.DELETION))));
    // null = no filter
    filter = ExecutionUtils.contributionsFilter(null);
    assertTrue(filter.test(createOSMContribution(EnumSet.of(ContributionType.CREATION))));
    assertTrue(filter.test(createOSMContribution(EnumSet.of(ContributionType.DELETION))));
  }

  @Test
  public void contributionsFilterInvalid() {
    assertThrows(BadRequestException.class, () -> ExecutionUtils.contributionsFilter("doesnotexist"));

  }

  private OSMContribution createOSMContribution(Set<ContributionType> contributionTypes) {
    return new DummyOSMContribution() {
      @Override
      public boolean is(ContributionType contributionType) {
        return contributionTypes.contains(contributionType);
      }

      @Override
      public EnumSet<ContributionType> getContributionTypes() {
        return EnumSet.copyOf(contributionTypes);
      }
    };
  }

  private static class DummyOSMContribution implements OSMContribution {
    @Override
    public OSHDBTimestamp getTimestamp() {
      return null;
    }

    @Override
    public Geometry getGeometryBefore() {
      return null;
    }

    @Override
    public Geometry getGeometryUnclippedBefore() {
      return null;
    }

    @Override
    public Geometry getGeometryAfter() {
      return null;
    }

    @Override
    public Geometry getGeometryUnclippedAfter() {
      return null;
    }

    @Override
    public OSMEntity getEntityBefore() {
      return null;
    }

    @Override
    public OSMEntity getEntityAfter() {
      return null;
    }

    @Override
    public OSHEntity getOSHEntity() {
      return null;
    }

    @Override
    public boolean is(ContributionType contributionType) {
      return false;
    }

    @Override
    public EnumSet<ContributionType> getContributionTypes() {
      return null;
    }

    @Override
    public int getContributorUserId() {
      return 0;
    }

    @Override
    public long getChangesetId() {
      return 0;
    }

    @Override
    public int compareTo(@NotNull OSMContribution contribution) {
      return 0;
    }
  }
}
