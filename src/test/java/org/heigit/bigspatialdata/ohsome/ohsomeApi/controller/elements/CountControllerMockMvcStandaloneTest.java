package org.heigit.bigspatialdata.ohsome.ohsomeApi.controller.elements;

import static org.mockito.BDDMockito.given;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.DefaultAggregationResponse;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.metadata.Metadata;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.result.Result;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import com.fasterxml.jackson.databind.ObjectMapper;

//@RunWith(MockitoJUnitRunner.class)
public class CountControllerMockMvcStandaloneTest {

//  private MockMvc mvc;
//
//  @Mock
//  private DefaultAggregationResponse response;
//  @InjectMocks
//  private CountController countController;
//  private JacksonTester<Result> json;
//
//  @Before
//  public void setup() {
//    // Initializes the JacksonTester
//    JacksonTester.initFields(this, new ObjectMapper());
//    // MockMvc standalone approach
//    mvc = MockMvcBuilders.standaloneSetup(countController).build();
//  }
//
//  @Test
//  public void canRetrieveSameResponse() throws Exception {
//
//    // definition of a mock-result
//    Result result = new Result("2017-01-01", 1.1);
//    Metadata metadata =
//        new Metadata(1, "unit", "description", "http://localhost:8080/elements/count?");
//    response =
//        new DefaultAggregationResponse("license", "copyright", metadata, new Result[] {result});
//
//    // given
//    given(response).willReturn(response);

//    // when
//    MockHttpServletResponse response =
//        mvc.perform(get("/superheroes/2").accept(MediaType.APPLICATION_JSON)).andReturn()
//            .getResponse();
//
//    // then
//    assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
//    assertThat(response.getContentAsString())
//        .isEqualTo(jsonSuperHero.write(new SuperHero("Rob", "Mannon", "RobotMan")).getJson());
//  }
}
