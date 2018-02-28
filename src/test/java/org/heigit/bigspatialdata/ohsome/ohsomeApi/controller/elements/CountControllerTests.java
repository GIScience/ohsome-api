package org.heigit.bigspatialdata.ohsome.ohsomeApi.controller.elements;

import org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.DefaultAggregationResponse;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.metadata.Metadata;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.result.Result;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@RunWith(SpringRunner.class)
@WebMvcTest(value = CountController.class)
public class CountControllerTests {

  @Autowired
  private MockMvc mockMvc;
  @MockBean
  private CountController countController;

  @Test
  public void getCount() throws Exception {

    // definition of a mock-result
    Result result = new Result("2017-01-01", 1.1);
    Metadata metadata =
        new Metadata(1, "unit", "description", "http://localhost:8080/elements/count?");
    DefaultAggregationResponse response =
        new DefaultAggregationResponse("license", "copyright", metadata, new Result[] {result});

    Mockito
        .when(countController.getCount("8.6128,49.3183,8.7294,49.4376", null, null,
            new String[] {"way"}, null, null, null, new String[] {"2017-01-01"}, "true"))
        .thenReturn(response);

    RequestBuilder requestBuilder = MockMvcRequestBuilders.get("http://localhost:8080/elements/count?")
        .param("bboxes", "8.6128,49.3183,8.7294,49.4376").param("types", "way")
        .param("time", "2017-01-01").param("showMetadata", "true")
        .accept(MediaType.APPLICATION_JSON);

    mockMvc.perform(requestBuilder).andExpect(MockMvcResultMatchers.status().isOk());
  }
}
