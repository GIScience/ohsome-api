package org.heigit.ohsome.ohsomeapi.utilities;

import org.heigit.ohsome.ohsomeapi.inputprocessing.InputProcessor;
import org.heigit.ohsome.ohsomeapi.output.Metadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MetadataUtility implements Utility {
  @Autowired
  InputProcessor inputProcessor;

  /**
   * Creates the metadata for the JSON response containing info like execution time, request URL and
   * a short description of the returned data.
   */
  public Metadata generateMetadata(String description) {
    final long startTime = System.currentTimeMillis();
    Metadata metadata = null;
    if (inputProcessor.getProcessingData().isShowMetadata()) {
      long duration = System.currentTimeMillis() - startTime;
      metadata = new Metadata(duration, description,
          inputProcessor.getRequestUrlIfGetRequest());
    }
    return metadata;
  }
}
