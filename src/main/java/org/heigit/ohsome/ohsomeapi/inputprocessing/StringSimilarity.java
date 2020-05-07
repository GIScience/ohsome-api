package org.heigit.ohsome.ohsomeapi.inputprocessing;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import org.apache.commons.text.similarity.FuzzyScore;

/**
 * Holds methods for computing the fuzzy string similarity between the query parameter and the
 * available parameter of the given resource.
 */
public class StringSimilarity {

  /**
   * Computes a string similarity (Fuzzy Score algorithm) between the requested parameter and the
   * possible parameters of the resource. It returns a String with a suggestion of possible
   * parameters with a fuzzy score higher than 4. Comparisons with fuzzy score lower than 5 are not
   * considered. If all comparisons give a fuzzy score lower than 5, it returns only a message of
   * "unknown parameter" without suggestions.
   * 
   * <p>
   * For example:
   * 
   * <p>
   * <ul>
   * <li>Given the false parameter "metadata" for the resource "groupByTag", it returns the
   * suggestions "showMetadata" and "timeout", which are the only two matches with a score higher
   * than 4.</li>
   * <li>Given the false parameter "groupByKe" for the resource "groupByKey", it returns the
   * suggestion "groupByKeys", which is the only parameter with a matching score higher than 4 valid
   * for this resource. Since the available parameter "groupByKey" is not valid for this resource,
   * it will not be taken in consideration as suggestion parameter for this resource.</li>
   * <li>Given the false parameter "qwerty" for the resource "elementsFullHistory/geometry", since
   * the matching score is lower than 5 in every match with the possible parameters, it returns only
   * a message of "unknown parameter" without suggestions.</li>
   * </ul>
   * 
   * <p>
   * <strong>Warning: Given typical mistyped parameters like "froupByKeys" or "fgroupByKeys" for the
   * resource "groupByKey", it returns only a message of "unknown parameter" without suggestions
   * despite the similarity to the right available parameter. This is because the Fuzzy Score
   * algorithm matches the chars of the two strings from left to right.</strong>
   */
  public static String findSimilarParameters(String parameter, String[] possibleParameters) {
    ArrayList<FuzzyScoreObject> list = new ArrayList<FuzzyScoreObject>();
    int i = 0;
    for (int k = 0; k < possibleParameters.length; k++) {
      double scoreDouble =
          new FuzzyScore(Locale.getDefault()).fuzzyScore(possibleParameters[k], parameter);
      BigDecimal fuzzyScore = new BigDecimal(scoreDouble);
      if (fuzzyScore.doubleValue() > 4) {
        FuzzyScoreObject object =
            new FuzzyScoreObject(parameter, fuzzyScore, possibleParameters[k]);
        list.add(i, object);
        i++;
      }
    }
    if (list.size() == 0) {
      return "Unknown parameter '" + parameter + "' for this resource.";
    }
    Collections.sort(list, FuzzyScoreObject.sortByScore);
    if (list.size() == 1) {
      String uniqueParameter = list.get(0).getPossibleParameter();
      return "Unknown parameter '" + parameter + "' for this resource. Did you mean '"
          + uniqueParameter + "'?";
    } else {
      list = sortParamsByLength(parameter, list);
      return "Unknown parameter '" + parameter + "' for this resource. Did you mean '"
          + list.get(0).getPossibleParameter() + "' or '" + list.get(1).getPossibleParameter()
          + "'?";
    }
  }

  /**
   * Sorts the suggested parameters, which are already sorted by the fuzzy matching score, by the
   * difference in length between the requested parameter and the suggested parameters.
   */
  private static ArrayList<FuzzyScoreObject> sortParamsByLength(String parameter,
      ArrayList<FuzzyScoreObject> list) {
    ArrayList<FuzzyScoreObject> list1 = new ArrayList<FuzzyScoreObject>();
    int diff1;
    int diff2;
    int lastIndex = list.size() - 1;
    if (list.get(lastIndex).getFuzzyScore() == list.get(lastIndex - 1).getFuzzyScore()) {
      diff1 = parameter.length() - list.get(lastIndex).getPossibleParameter().length();
      diff2 = parameter.length() - list.get(lastIndex - 1).getPossibleParameter().length();
      if (diff1 < 0) {
        diff1 *= -1;
      }
      if (diff2 < 0) {
        diff2 *= -1;
      }
      if (diff1 <= diff2) {
        list1.add(list.get(lastIndex));
        list1.add(list.get(lastIndex - 1));
      } else {
        list1.add(list.get(lastIndex - 1));
        list1.add(list.get(lastIndex));
      }
      return list1;
    } else {
      list1.add(list.get(lastIndex));
      list1.add(list.get(lastIndex - 1));
      return list1;
    }
  }
}

