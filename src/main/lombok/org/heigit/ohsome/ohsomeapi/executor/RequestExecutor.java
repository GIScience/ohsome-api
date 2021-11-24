package org.heigit.ohsome.ohsomeapi.executor;

import java.text.DecimalFormat;
import org.heigit.ohsome.ohsomeapi.oshdb.ExtractMetadata;
import org.heigit.ohsome.ohsomeapi.output.Attribution;
import org.springframework.stereotype.Component;

//@RequiredArgsConstructor
@Component
public abstract class RequestExecutor {

  public final String URL = ExtractMetadata.attributionUrl;
  public final String TEXT = ExtractMetadata.attributionShort;
  public final Attribution ATTRIBUTION = new Attribution(URL, TEXT);
  public final DecimalFormat df = ExecutionUtils.defineDecimalFormat("#.##");
//  protected final HttpServletRequest servletRequest;
//  protected final HttpServletResponse servletResponse;
}
