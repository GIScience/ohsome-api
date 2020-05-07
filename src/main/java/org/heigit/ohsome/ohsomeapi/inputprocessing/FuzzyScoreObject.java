package org.heigit.ohsome.ohsomeapi.inputprocessing;

import java.math.BigDecimal;
import java.util.Comparator;

/** Holds those variables which are relevant for the string similarity algorithm 'Fuzzy Score'. */
public class FuzzyScoreObject {

  private String queryParameter;
  private BigDecimal fuzzyScore;
  private String possibleParameter;

  /**
   * Creates an object with the relevant variables for the string similarity algorithm.
   */
  public FuzzyScoreObject(String queryParameter, BigDecimal fuzzyScore, String possibleParameter) {
    this.queryParameter = queryParameter;
    this.fuzzyScore = fuzzyScore;
    this.possibleParameter = possibleParameter;
  }

  /**
   * Sorts the request parameters by matching score.
   */
  public static Comparator<FuzzyScoreObject> sortByScore = new Comparator<FuzzyScoreObject>() {
    public int compare(FuzzyScoreObject o1, FuzzyScoreObject o2) {
      return o1.fuzzyScore.compareTo(o2.fuzzyScore);
    }
  };

  public String getQueryParameter() {
    return queryParameter;
  }

  public void setQueryParameter(String queryParameter) {
    this.queryParameter = queryParameter;
  }

  public BigDecimal getFuzzyScore() {
    return fuzzyScore;
  }

  public void setFuzzyScore(BigDecimal fuzzyScore) {
    this.fuzzyScore = fuzzyScore;
  }

  public String getPossibleParameter() {
    return possibleParameter;
  }

  public void setPossibleParameter(String possibleParameter) {
    this.possibleParameter = possibleParameter;
  }

  @Override
  public String toString() {
    return queryParameter + " " + fuzzyScore + " " + possibleParameter;
  }
}
