package org.heigit.bigspatialdata.ohsome.oshdbRestApi.controller.executor;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import org.heigit.bigspatialdata.ohsome.oshdbRestApi.Application;
import org.heigit.bigspatialdata.ohsome.oshdbRestApi.exception.BadRequestException;
import org.heigit.bigspatialdata.ohsome.oshdbRestApi.inputProcessing.BoundaryType;
import org.heigit.bigspatialdata.ohsome.oshdbRestApi.inputProcessing.GeometryBuilder;
import org.heigit.bigspatialdata.oshdb.util.exceptions.OSHDBKeytablesNotFoundException;
import org.heigit.bigspatialdata.oshdb.util.geometry.OSHDBGeometryBuilder;
import org.heigit.bigspatialdata.oshdb.util.tagtranslator.TagTranslator;
import com.vividsolutions.jts.geom.Geometry;

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

  /**
   * Evaluates the keys2 and values2 <code>String</code> arrays from /share requests.
   * 
   * @param keys2 <code>String</code> array containing the provided keys2 parameters.
   * @param values2 <code>String</code> array containing the provided values2 parameters.
   * @return <code>String</code> array containing the given (or an empty) values2 array, which is
   *         not null.
   */
  public String[] shareParamEvaluation(String[] keys2, String[] values2) {

    if (keys2 == null || keys2.length < 1)
      throw new BadRequestException(
          "You need to define at least one key if you want to use /share.");
    if (values2 == null)
      values2 = new String[0];
    if (keys2.length < values2.length)
      throw new BadRequestException(
          "There cannot be more input values in values2 than in keys2 as values2n must fit to keys2n.");

    return values2;
  }

  /**
   * Gets the geometry from the currently in-use boundary object(s).
   * 
   * @param boundary <code>BoundaryType</code> object (NOBOUNDARY, BBOXES, BCIRCLES, BPOLYS).
   * @param geomBuilder <code>GeometryBuilder</code> object.
   * @return <code>Geometry</code> object of the used boundary parameter.
   */
  public Geometry getGeometry(BoundaryType boundary, GeometryBuilder geomBuilder) {

    Geometry geom;
    switch (boundary) {
      case NOBOUNDARY:
        geom = OSHDBGeometryBuilder.getGeometry(geomBuilder.getBbox());
        break;
      case BBOXES:
        geom = OSHDBGeometryBuilder.getGeometry(geomBuilder.getBbox());
        break;
      case BCIRCLES:
        geom = geomBuilder.getbcircleGeom();
        break;
      case BPOLYS:
        geom = geomBuilder.getBpoly();
        break;
      default:
        geom = null;
    }

    return geom;
  }
}
