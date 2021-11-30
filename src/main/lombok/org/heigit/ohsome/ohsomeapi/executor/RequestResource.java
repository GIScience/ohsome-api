package org.heigit.ohsome.ohsomeapi.executor;

import lombok.Getter;
import org.springframework.stereotype.Component;

/**
 * Enumeration defining the request resource (LENGTH, PERIMETER, AREA, COUNT, GROUPBYTAG,
 * GROUPBYKEY, RATIO, DATAEXTRACTION, CONTRIBUTION).
 */
@Getter
//@AllArgsConstructor(access = AccessLevel.PACKAGE)
@Component
public class RequestResource {
//  LENGTH("length", "meters"), PERIMETER("perimeter", "meters"), AREA("area",
//      "square meters"), COUNT("count", "absolute values"), GROUPBYTAG("", ""), GROUPBYKEY("",
//          ""), RATIO("", ""), DATAEXTRACTION("OSM data as GeoJSON features.",
//              ""), DATAEXTRACTIONFFULLHISTORY("Full-history OSM data as GeoJSON features.",
//                  ""), CONTRIBUTIONS("Contributions as GeoJSON features.",
//                      ""), CONTRIBUTIONSLATEST("Latest contributions as GeoJSON features.", "");

  private final String[] LENGTH = {"length", "meters"};
  private final String[] PERIMETER = {"perimeter", "meters"};
  private final String[] AREA = {"area", "square meters"};
  private final String[] COUNT = {"count", "absolute values"};
  private final String[] GROUPBYTAG = {"", ""};
  private final String[] GROUPBYKEY = {"", ""};
  private final String[] RATIO = {"", ""};
  private final String[] DATAEXTRACTION = {"OSM data as GeoJSON features.", ""};
  private final String[] DATAEXTRACTIONFFULLHISTORY = {"Full-history OSM data as GeoJSON features.", ""};
  private final String[] CONTRIBUTIONS = {"Contributions as GeoJSON features.", ""};
  private final String[] CONTRIBUTIONSLATEST = {"Latest contributions as GeoJSON features.", ""};

  public String[] getLENGTH() {
    return LENGTH;
  }

  public String[] getPERIMETER() {
    return PERIMETER;
  }

  public String[] getAREA() {
    return AREA;
  }

  public String[] getCOUNT() {
    return COUNT;
  }

  public String[] getGROUPBYTAG() {
    return GROUPBYTAG;
  }

  public String[] getGROUPBYKEY() {
    return GROUPBYKEY;
  }

  public String[] getRATIO() {
    return RATIO;
  }

  public String[] getDATAEXTRACTION() {
    return DATAEXTRACTION;
  }

  public String[] getDATAEXTRACTIONFFULLHISTORY() {
    return DATAEXTRACTIONFFULLHISTORY;
  }

  public String[] getCONTRIBUTIONS() {
    return CONTRIBUTIONS;
  }

  public String[] getCONTRIBUTIONSLATEST() {
    return CONTRIBUTIONSLATEST;
  }

  //  private final String description;
//  private final String unit;

}
