package org.heigit.ohsome.ohsomeapi.inputprocessing;

import java.math.BigDecimal;
import java.util.Comparator;
import lombok.Getter;

/** Holds those variables which are relevant for the string similarity algorithm 'Fuzzy Score'. */
@Getter
public class FuzzyScoreObject {

  private final BigDecimal fuzzyScore;
  private final String possibleParameter;

  /**
   * Creates an object with the relevant variables for the string similarity algorithm.
   */
  public FuzzyScoreObject(BigDecimal fuzzyScore, String possibleParameter) {
    this.fuzzyScore = fuzzyScore;
    this.possibleParameter = possibleParameter;
  }

  /**
   * Sorts the request parameters by matching score.
   */
  static Comparator<FuzzyScoreObject> sortByScore =
      (o1, o2) -> o1.fuzzyScore.compareTo(o2.fuzzyScore);
}
