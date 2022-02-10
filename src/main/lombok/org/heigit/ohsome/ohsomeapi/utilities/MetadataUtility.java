package org.heigit.ohsome.ohsomeapi.utilities;

import org.heigit.ohsome.ohsomeapi.inputprocessing.InputProcessor;
import org.heigit.ohsome.ohsomeapi.output.Metadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MetadataUtility {

  @Autowired
  private StartTimeOfRequest startTimeOfRequest;

  /**
   * Creates the metadata for the JSON response containing info like execution time, request URL and
   * a short description of the returned data.
   */
  public Metadata generateMetadata(String description, InputProcessor inputProcessor) {
    Metadata metadata = null;
    if (inputProcessor.getProcessingData().isShowMetadata()) {
      long duration = System.currentTimeMillis() - startTimeOfRequest.getSTART_TIME();
      metadata = new Metadata(duration, description,
          inputProcessor.getRequestUrlIfGetRequest());
    }
    return metadata;
  }
}
