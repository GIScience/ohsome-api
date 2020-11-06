package org.heigit.ohsome.ohsomeapi.executor;

import java.text.DecimalFormat;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.heigit.ohsome.ohsomeapi.oshdb.ExtractMetadata;
import org.heigit.ohsome.ohsomeapi.output.dataaggregationresponse.Attribution;

@RequiredArgsConstructor
public abstract class RequestExecutor {

  protected static final String URL = ExtractMetadata.attributionUrl;
  protected static final String TEXT = ExtractMetadata.attributionShort;
  protected static final Attribution ATTRIBUTION = new Attribution(URL, TEXT);
  public static final DecimalFormat df = ExecutionUtils.defineDecimalFormat("#.##");
  protected final HttpServletRequest servletRequest;
  protected final HttpServletResponse servletResponse;
}
