package org.heigit.bigspatialdata.ohsome.ohsomeapi.inputprocessing;

import static org.junit.Assert.assertTrue;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.exception.BadRequestException;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.oshdb.ExtractMetadata;
import org.junit.Before;
import org.junit.Test;

/**
 * Test class for the
 * {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.inputprocessing.InputProcessingUtils
 * InputProcessingUtils} class.
 */
public class InputProcessingUtilsTest {
  
  private InputProcessingUtils inProUtils;

  @Before
  public void setup() {
    inProUtils = new InputProcessingUtils();
    ExtractMetadata.fromTstamp = "2007-11-01";
    ExtractMetadata.toTstamp = "2018-01-01T00:00:00";
  }
  
  // time tests

  @Test(expected = BadRequestException.class)
  public void provideNonIsoConformTimeParameter() throws Exception {
    String time = "2015-01-01/2016-01-01/[invalid input]";
    inProUtils.extractIsoTime(time);
  }
  
  @Test
  public void provideIsoConformTimeParameter() throws Exception {
    String time = "2015-01-01/2017-01-01/P1Y";
    String[] timeVals = inProUtils.extractIsoTime(time);
    assertTrue(timeVals.length == 3);
    assertTrue(timeVals[0].equals("2015-01-01"));
    assertTrue(timeVals[1].equals("2017-01-01"));
    assertTrue(timeVals[2].equals("P1Y"));
  }
}
