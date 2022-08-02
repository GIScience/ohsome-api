package org.heigit.ohsome.ohsomeapi.inputprocessing;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import org.heigit.ohsome.ohsomeapi.controller.TestProperties;
import org.heigit.ohsome.ohsomeapi.exception.BadRequestException;
import org.heigit.ohsome.ohsomeapi.oshdb.ExtractMetadata;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


/**
 * Test class for the
 * {@link org.heigit.ohsome.ohsomeapi.inputprocessing.InputProcessingUtils
 * InputProcessingUtils} class.
 */
public class InputProcessingUtilsTest {

  private InputProcessingUtils inProUtils;

  /** Checks the value of the junit property. */
  @BeforeAll
  public static void checkJunitProperty() {
    assumeTrue(TestProperties.JUNIT == null || !TestProperties.JUNIT.equalsIgnoreCase("no"));
  }

  @BeforeEach
  public void setup() {
    inProUtils = new InputProcessingUtils();
    ExtractMetadata.fromTstamp = "2008-01-01T00:00:00Z";
    ExtractMetadata.toTstamp = "2018-01-01T00:00:00Z";
  }

  // boundary param split tests

  @Test
  public void splitBboxesParam() throws Exception {
    String bboxes = "A:8.67567,49.40695,8.69434,49.40882|B:8.67568,49.40694,8.69433,49.40881";
    assertTrue(inProUtils.splitBboxes(bboxes).get(5).equals("49.40694"));
  }

  @Test
  public void splitBcirclesParam() throws Exception {
    String bcircles = "A:8.6528,49.3683,1000|B:8.7294,49.4376,1000";
    assertTrue(inProUtils.splitBcircles(bcircles).get(4).equals("49.4376"));
  }

  @Test
  public void splitBpolysParam() throws Exception {
    String bpolys = "A:8.5992,49.3567,8.7499,49.4371,8.7499,49.4379,8.5992,49.3567|"
        + "B:9.1638,49.113,9.2672,49.1766,9.2672,49.1775,9.1638,49.113";
    assertTrue(inProUtils.splitBpolys(bpolys).get(9).equals("49.113"));
  }

  @Test
  public void splitBboxesParamWithAndWithoutColon() throws Exception {
    String bboxes = "A:8.67567,49.40695,8.69434,49.40882|[idWithoutColon]8.67568,49.40694,"
        + "8.69433,49.40881";
    assertThrows(BadRequestException.class, () -> inProUtils.splitBboxes(bboxes));
  }

  @Test
  public void splitBcirclesParamWithAndWithoutColon() throws Exception {
    String bcircles = "A:8.6528,49.3683,1000|[idWithoutColon]8.7294,49.4376,1000";
    assertThrows(BadRequestException.class, () -> inProUtils.splitBcircles(bcircles));
  }

  @Test
  public void splitBpolysParamWithAndWithoutColon() throws Exception {
    String bpolys = "A:8.5992,49.3567,8.7499,49.4371,8.7499,49.4379,8.5992,49.3567|"
        + "[idWithoutColon]9.1638,49.113,9.2672,49.1766,9.2672,49.1775,9.1638,49.113";
    assertThrows(BadRequestException.class, () -> inProUtils.splitBpolys(bpolys));
  }

  @Test
  public void splitBboxesParamWithoutPipe() throws Exception {
    String bboxes = "A:8.67567,49.40695,8.69434,49.40882,B:8.67568,49.40694,8.69433,49.40881";
    assertThrows(BadRequestException.class, () -> inProUtils.splitBboxes(bboxes));
  }

  @Test
  public void splitBcirclesParamWithoutPipe() throws Exception {
    String bcircles = "A:8.6528,49.3683,1000,B:8.7294,49.4376,1000";
    assertThrows(BadRequestException.class, () -> inProUtils.splitBcircles(bcircles));
  }

  @Test
  public void splitBpolysParamWithoutPipe() throws Exception {
    String bpolys = "A:8.5992,49.3567,8.7499,49.4371,8.7499,49.4379,8.5992,49.3567,B:9.1638,49.113,"
        + "9.2672,49.1766,9.2672,49.1775,9.1638,49.113";
    assertThrows(BadRequestException.class, () -> inProUtils.splitBpolys(bpolys));
  }

  // time tests

  @Test
  public void provideNonIsoConformTimeParameter() throws Exception {
    String time = "2015-01-01/2016-01-01/[invalid input]";
    assertThrows(BadRequestException.class, () -> inProUtils.extractIsoTime(time));
  }

  @Test
  public void provideIsoConformTimeParameter() throws Exception {
    String time = "2015-01-01/2017-01-01/P1Y";
    String[] timeVals = inProUtils.extractIsoTime(time);
    assertTrue(timeVals.length == 3);
    assertTrue(timeVals[0].equals("2015-01-01T00:00:00Z"));
    assertTrue(timeVals[1].equals("2017-01-01T00:00:00Z"));
    assertTrue(timeVals[2].equals("P1Y"));
  }
}
