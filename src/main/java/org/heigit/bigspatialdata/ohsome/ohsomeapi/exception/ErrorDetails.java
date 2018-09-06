package org.heigit.bigspatialdata.ohsome.ohsomeapi.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class ErrorDetails {
  private String timestamp;
  private int status;
  private String message;
  private String requestUrl;

  public ErrorDetails(String timestamp, int status, String message, String requestUrl) {
    super();
    this.timestamp = timestamp;
    this.status = status;
    this.message = message;
    this.requestUrl = requestUrl;
  }

  public String getTimestamp() {
    return timestamp;
  }

  public int getStatus() {
    return status;
  }


  public String getMessage() {
    return message;
  }

  public String getRequestUrl() {
    return requestUrl;
  }
}
