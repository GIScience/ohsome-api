package org.heigit.ohsome.ohsomeapi.controller.filter;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.heigit.ohsome.ohsomeapi.exception.BadRequestException;
import org.heigit.ohsome.ohsomeapi.exception.ExceptionMessages;
import org.heigit.ohsome.ohsomeapi.oshdb.DbConnData;
import org.heigit.ohsome.oshdb.filter.FilterExpression;
import org.heigit.ohsome.oshdb.filter.FilterParser;
import org.jparsec.error.ParserException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for endpoints in "/filter".
 */
@Api(tags = "Filter")
@RestController
@RequestMapping("/filter")
public class ValidateController {
  /**
   * Validates a provided filter string: Returns 200 OK if the syntax is valid
   * and an HTTP 400 error code otherwise.
   *
   * @param servletRequest <code>HttpServletRequest</code> of the incoming request
   * @param servletResponse <code>HttpServletResponse</code> of the outgoing response
   * @throws BadRequestException if a te filter cannot be parsed, the error object contains a
   *         message about the potential causes of the invalidity of the filter.
   * @return if the filter is valid: the originally supplied filter
   */
  @ApiOperation(nickname = "Filter Validator", value = "Checks a given ohsome filter string for syntax errors.")
  @RequestMapping(value = "/validate", method = {RequestMethod.GET, RequestMethod.POST},
      produces = {"text/plain", "application/json"})
  public String validate(HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) {
    FilterParser fp = new FilterParser(DbConnData.tagTranslator, true);
    String filter = servletRequest.getParameter("filter");
    if (filter == null || filter.isEmpty()) {
      throw new BadRequestException("No filter parameter provided.");
    }
    try {
      //noinspection ResultOfMethodCallIgnored
      fp.parse(filter);
      return filter;
    } catch (ParserException ex) {
      throw new BadRequestException(ExceptionMessages.FILTER_SYNTAX + " Detailed error message: "
          + ex.getMessage().replace("\n", " "));
    }
  }
}
