package org.heigit.ohsome.ohsomeapi.utilities;

import org.heigit.ohsome.ohsomeapi.inputprocessing.InputProcessor;
import org.heigit.ohsome.ohsomeapi.output.Metadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@Component
@RequestScope
public class MetadataUtility {

  private final StartTimeOfRequest startTimeOfRequest;

  @Autowired
  public MetadataUtility(StartTimeOfRequest startTimeOfRequest) {
    this.startTimeOfRequest = startTimeOfRequest;
  }

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
