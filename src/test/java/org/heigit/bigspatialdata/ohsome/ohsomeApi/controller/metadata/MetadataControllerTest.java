package org.heigit.bigspatialdata.ohsome.ohsomeApi.controller.metadata;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Test class for the
 * {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.controller.metadata.MetadataController
 * MetadataController} class.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class MetadataControllerTest {

  @Autowired
  private MockMvc mvc;
  
  @Test
  public void getMetadataTest() throws Exception {
      this.mvc.perform(get("/metadata")).andExpect(status().isOk());
  }

}
