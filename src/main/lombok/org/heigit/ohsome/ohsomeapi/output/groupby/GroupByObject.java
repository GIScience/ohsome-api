package org.heigit.ohsome.ohsomeapi.output.groupby;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Abstract class used by the following groupByResult classes.
 *
 * <ul>
 * <li>{@link org.heigit.ohsome.ohsomeapi.output.groupby.GroupByResult GroupByResult}</li>
 * <li>{@link org.heigit.ohsome.ohsomeapi.output.ratio.RatioGroupByResult RatioGroupByResult}</li>
 * </ul>
 */
@Getter
@AllArgsConstructor
public abstract class GroupByObject {

  @ApiModelProperty(notes = "Object on which the results are grouped on", required = true)
  protected Object groupByObject;
}
