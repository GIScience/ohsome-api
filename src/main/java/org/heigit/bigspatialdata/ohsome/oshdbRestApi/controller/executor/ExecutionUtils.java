package org.heigit.bigspatialdata.ohsome.oshdbRestApi.controller.executor;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import org.heigit.bigspatialdata.ohsome.oshdbRestApi.Application;
import org.heigit.bigspatialdata.oshdb.util.exceptions.OSHDBKeytablesNotFoundException;
import org.heigit.bigspatialdata.oshdb.util.tagtranslator.TagTranslator;

public class ExecutionUtils {

  /**
   * Defines a certain decimal format.
   * 
   * @param format <code>String</code> defining the format (e.g.: "#.####" for getting 4 digits
   *        after the comma)
   * @return <code>DecimalFormat</code> object with the defined format.
   */
  public DecimalFormat defineDecimalFormat(String format) {
    DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.getDefault());
    otherSymbols.setDecimalSeparator('.');
    DecimalFormat decForm = new DecimalFormat(format, otherSymbols);
    return decForm;
  }

  /**
   * Creates a TagTranslator object from the given keytables or oshdb file.
   * 
   * @return <code>TagTranslator</code> object.
   * @throws OSHDBKeytablesNotFoundException
   */
  public TagTranslator createTagTranslator() throws OSHDBKeytablesNotFoundException {
    TagTranslator tt;
    if (Application.getH2Db() == null)
      tt = new TagTranslator(Application.getKeytables().getConnection());
    else
      tt = new TagTranslator(Application.getH2Db().getConnection());

    return tt;
  }

}
