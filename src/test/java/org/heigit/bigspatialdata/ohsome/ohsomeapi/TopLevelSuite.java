package org.heigit.bigspatialdata.ohsome.ohsomeapi;

import org.heigit.bigspatialdata.ohsome.ohsomeapi.controller.dataaggregation.GetControllerTest;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.controller.dataaggregation.PostControllerTest;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.inputprocessing.GeometryBuilderTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/** Suite class to define the order in which the test classes get executed. */
@SuiteClasses({GetControllerTest.class, PostControllerTest.class, GeometryBuilderTest.class})
@RunWith(Suite.class)
public class TopLevelSuite {
}
