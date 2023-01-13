package org.heigit.ohsome.ohsomeapi.inputprocessing;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.apache.commons.text.similarity.FuzzyScore;

/**
 * Holds methods for computing the fuzzy string similarity between the query parameter and the
 * available parameter of the given resource.
 */
public class StringSimilarity {
  private StringSimilarity() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * Computes a string similarity (Fuzzy Score algorithm) between the requested parameter and the
   * possible parameters of the resource. It returns a String with a suggestion of possible
   * parameters with a fuzzy score higher than 4. Comparisons with fuzzy score lower than 5 are not
   * considered. If all comparisons give a fuzzy score lower than 5, it returns only a message of
   * "unknown parameter" without suggestions.
   *
   * <p>For example:
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
   * <p><strong>Warning: Given typical mistyped parameters like "froupByKeys" or "fgroupByKeys" for
   * the resource "groupByKey", it returns only a message of "unknown parameter" without suggestions
   * despite the similarity to the right available parameter. This is because the Fuzzy Score
   * algorithm matches the chars of the two strings from left to right.</strong>
   *
   * @param parameter - request parameter to compare
   * @param possibleParameters - list of possible parameters
   * @return a String containing up to two suggested parameter if available.
   */
  public static String findSimilarParameters(String parameter, List<String> possibleParameters) {
    ArrayList<FuzzyScoreObject> scoreHigherThanFour = new ArrayList<>();
    int i = 0;
    final String unknownParam = "Unknown parameter '";
    for (String possibleParameter : possibleParameters) {
      double scoreDouble =
          new FuzzyScore(Locale.getDefault()).fuzzyScore(possibleParameter, parameter);
      BigDecimal fuzzyScore = BigDecimal.valueOf(scoreDouble);
      if (fuzzyScore.doubleValue() > 4) {
        FuzzyScoreObject object = new FuzzyScoreObject(fuzzyScore, possibleParameter);
        scoreHigherThanFour.add(i, object);
        i++;
      }
    }
    if (scoreHigherThanFour.isEmpty()) {
      return unknownParam + parameter + "' for this resource.";
    }
    scoreHigherThanFour.sort(FuzzyScoreObject.sortByScore);
    if (scoreHigherThanFour.size() == 1) {
      String uniqueParameter = scoreHigherThanFour.get(0).getPossibleParameter();
      return unknownParam + parameter + "' for this resource. Did you mean '" + uniqueParameter
          + "'?";
    } else {
      ArrayList<FuzzyScoreObject> possibleParams =
          sortParamsByLength(parameter, scoreHigherThanFour);
      return unknownParam + parameter + "' for this resource. Did you mean '"
          + possibleParams.get(0).getPossibleParameter() + "' or '"
          + possibleParams.get(1).getPossibleParameter() + "'?";
    }
  }

  /**
   * Sorts the suggested parameters, which are already sorted by the fuzzy matching score, by the
   * difference in length between the requested parameter and the suggested parameters.
   *
   * @param parameter - request parameter
   * @param possibleParams - list of possible parameters
   * @return an ArrayList of suggested parameters sorted by the difference in length between the
   *        requested parameter and the suggested parameters.
   */
  private static ArrayList<FuzzyScoreObject> sortParamsByLength(String parameter,
      ArrayList<FuzzyScoreObject> possibleParams) {
    ArrayList<FuzzyScoreObject> sortedParams = new ArrayList<>();
    int diffLastIndex;
    int diffSecondToLastIndex;
    int lastIndex = possibleParams.size() - 1;
    if (possibleParams.get(lastIndex).getFuzzyScore()
        .compareTo(possibleParams.get(lastIndex - 1).getFuzzyScore()) == 0) {
      diffLastIndex =
          parameter.length() - possibleParams.get(lastIndex).getPossibleParameter().length();
      diffSecondToLastIndex =
          parameter.length() - possibleParams.get(lastIndex - 1).getPossibleParameter().length();
      if (diffLastIndex < 0) {
        diffLastIndex *= -1;
      }
      if (diffSecondToLastIndex < 0) {
        diffSecondToLastIndex *= -1;
      }
      if (diffLastIndex <= diffSecondToLastIndex) {
        sortedParams.add(possibleParams.get(lastIndex));
        sortedParams.add(possibleParams.get(lastIndex - 1));
      } else {
        sortedParams.add(possibleParams.get(lastIndex - 1));
        sortedParams.add(possibleParams.get(lastIndex));
      }
      return sortedParams;
    } else {
      sortedParams.add(possibleParams.get(lastIndex));
      sortedParams.add(possibleParams.get(lastIndex - 1));
      return sortedParams;
    }
  }
}

