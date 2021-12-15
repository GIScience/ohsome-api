package org.heigit.ohsome.ohsomeapi.utilities;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import lombok.Getter;
import org.springframework.stereotype.Component;

@Component
public class DecimalFormatDefiner {

  @Getter
  private final DecimalFormat decimalFormat = defineDecimalFormat("#.##");
  @Getter
  private final DecimalFormat decimalFormatForRatioRequests = defineDecimalFormat("#.######");

  /**
   * Defines a certain decimal format.
   *
   * @param format <code>String</code> defining the format (e.g.: "#.####" for getting 4 digits
   *        after the comma)
   * @return <code>DecimalFormatDefiner</code> object with the defined format.
   */
  private DecimalFormat defineDecimalFormat(String format) {
    DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.getDefault());
    otherSymbols.setDecimalSeparator('.');
    return new DecimalFormat(format, otherSymbols);
  }
}
