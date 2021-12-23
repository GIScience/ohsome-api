package org.heigit.ohsome.ohsomeapi.utilities;

import org.heigit.ohsome.ohsomeapi.exception.BadRequestException;
import org.heigit.ohsome.ohsomeapi.exception.ExceptionMessages;
import org.heigit.ohsome.oshdb.filter.FilterExpression;
import org.heigit.ohsome.oshdb.filter.FilterParser;
import org.jparsec.error.ParserException;
import org.springframework.stereotype.Component;

@Component
public class FilterUtility {

  /**
   * Checks the given filter parameter if it's null or blank. Currently used for filter2 parameter
   * of /ratio processing.
   *
   * @param filter parameter to be checked
   * @throws BadRequestException if the given filter parameter is null or blank.
   */
  public void checkFilter(String filter2) {
    if (filter2.isBlank()) {
      throw new BadRequestException(
          "The filter2 parameter has to be defined when using a /ratio endpoint.");
    }
  }

  /** Combines the two given filters with an OR operation. Used in /ratio computation. */
  public String combineFiltersWithOr(String firstFilter, String secondFilter) {
    if (firstFilter.isBlank() || secondFilter.isBlank()) {
      // definition of an empty combined filter if filter1 or filter2 is empty
      return "";
    }
    return "(" + firstFilter + ") or (" + secondFilter + ")";
  }

  /**
   * Tries to parse the given filter using the given parser.
   *
   * @throws BadRequestException if the filter contains wrong syntax.
   */
  public FilterExpression parseFilter(FilterParser fp, String filter) {
    try {
      return fp.parse(filter);
    } catch (ParserException ex) {
      throw new BadRequestException(ExceptionMessages.FILTER_SYNTAX + " Detailed error message: "
          + ex.getMessage().replace("\n", " "));
    }
  }

}
