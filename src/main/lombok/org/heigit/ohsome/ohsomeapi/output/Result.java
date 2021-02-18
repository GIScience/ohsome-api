package org.heigit.ohsome.ohsomeapi.output;

/**
 * Interface for two Result objects.
 * <ul>
 * <li>{@link org.heigit.ohsome.ohsomeapi.output.elements.ElementsResult
 * ElementsResult}</li>
 * <li>{@link org.heigit.ohsome.ohsomeapi.output.ratio.RatioResult
 * RatioResult}</li>
 * </ul>
 */
public interface Result {

  double getValue();
}
