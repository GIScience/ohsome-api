package org.heigit.ohsome.ohsomeapi.refactoring;

import javax.servlet.http.HttpServletRequest;
import org.heigit.ohsome.ohsomeapi.output.Description;
import org.heigit.ohsome.ohsomeapi.output.Metadata;
import org.springframework.beans.factory.annotation.Autowired;

public class ParameterUtility {
@Autowired
  HttpServletRequest request;

  public Metadata getMetadata(long startTime) {
    Metadata metadata = null;
    if (request.getParameter("showMetadata").equals(true)) {
      long duration = System.currentTimeMillis() - startTime;
      String description;
      //TODO requestParameters.isDensity() instead of false
      description = Description.countContributions(false);
      metadata = new Metadata(duration, description, request.getRequestURL().toString());

      //TODO inputProcessor.getRequestUrlIfGetRequest(servletRequest) checks if it is a get request.
      // request.getRequestURL().toString() does not check it);
    }
    return metadata;
  }
}
