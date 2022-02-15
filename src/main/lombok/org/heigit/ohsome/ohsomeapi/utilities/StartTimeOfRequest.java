package org.heigit.ohsome.ohsomeapi.utilities;

import lombok.Getter;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@Component
@RequestScope
@Getter
public class StartTimeOfRequest {

  private final long START_TIME = System.currentTimeMillis();
}
