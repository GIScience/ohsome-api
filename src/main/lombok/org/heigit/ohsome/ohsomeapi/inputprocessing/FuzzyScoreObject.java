package org.heigit.ohsome.ohsomeapi.inputprocessing;

import java.math.BigDecimal;
import java.util.Comparator;
import lombok.AllArgsConstructor;
import lombok.Getter;

/** Holds those variables which are relevant for the string similarity algorithm 'Fuzzy Score'. */
@Getter
@AllArgsConstructor
public class FuzzyScoreObject {

  private final BigDecimal fuzzyScore;
  private final String possibleParameter;

  /**
   * Sorts the request parameters by matching score.
   */
  static Comparator<FuzzyScoreObject> sortByScore =
      (o1, o2) -> o1.fuzzyScore.compareTo(o2.fuzzyScore);
}
