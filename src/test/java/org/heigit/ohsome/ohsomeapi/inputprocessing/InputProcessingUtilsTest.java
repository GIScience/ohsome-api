package org.heigit.ohsome.ohsomeapi.inputprocessing;

import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import org.heigit.ohsome.ohsomeapi.controller.TestProperties;
import org.heigit.ohsome.ohsomeapi.exception.BadRequestException;
import org.heigit.ohsome.ohsomeapi.oshdb.ExtractMetadata;
import org.heigit.ohsome.ohsomeapi.utilities.SpatialUtility;
import org.heigit.ohsome.ohsomeapi.utilities.TimeUtility;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Test class for the
 * {@link org.heigit.ohsome.ohsomeapi.inputprocessing.InputProcessingUtils
 * InputProcessingUtils} class.
 */
public class InputProcessingUtilsTest {

  @Autowired
  private SpatialUtility spatialUtility;
  @Autowired
  private TimeUtility timeUtility;
  @Autowired
  private ExtractMetadata extractMetadata;

  /** Checks the value of the junit property. */
  @BeforeClass
  public static void checkJunitProperty() {
    assumeTrue(TestProperties.JUNIT == null || !TestProperties.JUNIT.equalsIgnoreCase("no"));
  }

  @Before
  public void setup() {
    //inProUtils = new InputProcessingUtils();
    extractMetadata.setFromTstamp("2008-01-01T00:00:00Z");
    extractMetadata.setToTstamp("2018-01-01T00:00:00Z");
  }

  // boundary param split tests

  @Test
  public void splitBboxesParam() throws Exception {
    String bboxes = "A:8.67567,49.40695,8.69434,49.40882|B:8.67568,49.40694,8.69433,49.40881";
    assertTrue(spatialUtility.splitBboxes(bboxes).get(5).equals("49.40694"));
  }

  @Test
  public void splitBcirclesParam() throws Exception {
    String bcircles = "A:8.6528,49.3683,1000|B:8.7294,49.4376,1000";
    assertTrue(spatialUtility.splitBcircles(bcircles).get(4).equals("49.4376"));
  }

  @Test
  public void splitBpolysParam() throws Exception {
    String bpolys = "A:8.5992,49.3567,8.7499,49.4371,8.7499,49.4379,8.5992,49.3567|"
        + "B:9.1638,49.113,9.2672,49.1766,9.2672,49.1775,9.1638,49.113";
    assertTrue(spatialUtility.splitBpolys(bpolys).get(9).equals("49.113"));
  }

  @Test(expected = BadRequestException.class)
  public void splitBboxesParamWithAndWithoutColon() throws Exception {
    String bboxes = "A:8.67567,49.40695,8.69434,49.40882|[idWithoutColon]8.67568,49.40694,"
        + "8.69433,49.40881";
    spatialUtility.splitBboxes(bboxes);
  }

  @Test(expected = BadRequestException.class)
  public void splitBcirclesParamWithAndWithoutColon() throws Exception {
    String bcircles = "A:8.6528,49.3683,1000|[idWithoutColon]8.7294,49.4376,1000";
    spatialUtility.splitBcircles(bcircles);
  }

  @Test(expected = BadRequestException.class)
  public void splitBpolysParamWithAndWithoutColon() throws Exception {
    String bpolys = "A:8.5992,49.3567,8.7499,49.4371,8.7499,49.4379,8.5992,49.3567|"
        + "[idWithoutColon]9.1638,49.113,9.2672,49.1766,9.2672,49.1775,9.1638,49.113";
    spatialUtility.splitBpolys(bpolys);
  }

  @Test(expected = BadRequestException.class)
  public void splitBboxesParamWithoutPipe() throws Exception {
    String bboxes = "A:8.67567,49.40695,8.69434,49.40882,B:8.67568,49.40694,8.69433,49.40881";
    spatialUtility.splitBboxes(bboxes);
  }

  @Test(expected = BadRequestException.class)
  public void splitBcirclesParamWithoutPipe() throws Exception {
    String bcircles = "A:8.6528,49.3683,1000,B:8.7294,49.4376,1000";
    spatialUtility.splitBcircles(bcircles);
  }

  @Test(expected = BadRequestException.class)
  public void splitBpolysParamWithoutPipe() throws Exception {
    String bpolys = "A:8.5992,49.3567,8.7499,49.4371,8.7499,49.4379,8.5992,49.3567,B:9.1638,49.113,"
        + "9.2672,49.1766,9.2672,49.1775,9.1638,49.113";
    spatialUtility.splitBpolys(bpolys);
  }

  // time tests

  @Test(expected = BadRequestException.class)
  public void provideNonIsoConformTimeParameter() throws Exception {
    String time = "2015-01-01/2016-01-01/[invalid input]";
    timeUtility.extractIsoTime(time);
  }

  @Test
  public void provideIsoConformTimeParameter() throws Exception {
    String time = "2015-01-01/2017-01-01/P1Y";
    String[] timeVals = timeUtility.extractIsoTime(time);
    assertTrue(timeVals.length == 3);
    assertTrue(timeVals[0].equals("2015-01-01T00:00:00Z"));
    assertTrue(timeVals[1].equals("2017-01-01T00:00:00Z"));
    assertTrue(timeVals[2].equals("P1Y"));
  }
}
