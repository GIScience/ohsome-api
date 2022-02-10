package org.heigit.ohsome.ohsomeapi.output;

import com.opencsv.CSVWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.heigit.ohsome.ohsomeapi.inputprocessing.InputProcessor;
import org.heigit.ohsome.ohsomeapi.oshdb.ExtractMetadata;
import org.heigit.ohsome.ohsomeapi.output.contributions.ContributionsResult;
import org.heigit.ohsome.ohsomeapi.output.elements.ElementsResult;
import org.heigit.ohsome.ohsomeapi.output.groupby.GroupByObject;
import org.heigit.ohsome.ohsomeapi.output.groupby.GroupByResult;
import org.heigit.ohsome.ohsomeapi.output.ratio.RatioGroupByResult;
import org.heigit.ohsome.ohsomeapi.output.ratio.RatioResult;
import org.heigit.ohsome.ohsomeapi.utilities.RequestUtils;
import org.springframework.beans.factory.annotation.Autowired;

public class CSVResponse {

  @Autowired
  private InputProcessor inputProcessor;
  @Autowired
  private HttpServletResponse servletResponse;
  @Autowired
  private ExtractMetadata extractMetadata;
  @Autowired
  private String ohsomeAPIVersion;

  /**
   * Writes a response in the csv format for /count|length|perimeter|area(/density)(/ratio)|groupBy
   * requests.
   */
  private Consumer<CSVWriter> writeCsvResponse(Object[] resultSet) {
    return writer -> writeCsvResponse(writer, resultSet);
  }

  /**
   * Writing of the CSV response for different types of result sets.
   */
  private void writeCsvResponse(CSVWriter writer, Object[] resultSet) {
    if (resultSet instanceof ElementsResult[]) {
      ElementsResult[] rs = (ElementsResult[]) resultSet;
      writer.writeNext(new String[] {"timestamp", "value"}, false);
      for (ElementsResult elementsResult : rs) {
        writer.writeNext(new String[] {elementsResult.getTimestamp(),
            String.valueOf(elementsResult.getValue())});
      }
    } else if (resultSet instanceof ContributionsResult[]) {
      ContributionsResult[] rs = (ContributionsResult[]) resultSet;
      writer.writeNext(new String[] {"fromTimestamp", "toTimestamp", "value"}, false);
      for (ContributionsResult contributionsResult : rs) {
        writer.writeNext(new String[] {
            contributionsResult.getFromTimestamp(),
            contributionsResult.getToTimestamp(),
            String.valueOf(contributionsResult.getValue())
        });
      }
    } else if (resultSet instanceof RatioResult[]) {
      RatioResult[] rs = (RatioResult[]) resultSet;
      writer.writeNext(new String[] {"timestamp", "value", "value2", "ratio"}, false);
      for (RatioResult ratioResult : rs) {
        writer.writeNext(
            new String[] {ratioResult.getTimestamp(), String.valueOf(ratioResult.getValue()),
                  String.valueOf(ratioResult.getValue2()), String.valueOf(ratioResult.getRatio())});
      }
    } else if (resultSet instanceof GroupByResult[]) {
      GroupByObject[] rs = (GroupByResult[]) resultSet;
      if (resultSet.length == 0) {
        writer.writeNext(new String[] {"timestamp"}, false);
      } else {
        var rows = createCsvResponseForElementsGroupBy(rs);
        writer.writeNext(rows.getLeft().toArray(new String[rows.getLeft().size()]), false);
        writer.writeAll(rows.getRight(), false);
      }
    }
  }

  /**
   * Defines character encoding, content type and cache header in given servlet response object.
   */
  private void setCsvSettingsInServletResponse() {
    servletResponse.setCharacterEncoding("UTF-8");
    servletResponse.setContentType("text/csv");
    if (!RequestUtils.cacheNotAllowed(inputProcessor.getProcessingData().getRequestUrl(),
        inputProcessor.getTime())) {
      servletResponse.setHeader("Cache-Control", "no-transform, public, max-age=31556926");
    }
  }

  /**
   * Writes the CSV response directly and returns a null Response as writer has already been called.
   *
   * @throws IOException thrown by {@link javax.servlet.ServletResponse#getWriter() getWriter}
   */
  private Response writeCsv(List<String[]> comments, Consumer<CSVWriter> consumer)
        throws IOException {
    setCsvSettingsInServletResponse();
    try (CSVWriter writer =
          new CSVWriter(servletResponse.getWriter(), ';', CSVWriter.DEFAULT_QUOTE_CHARACTER,
            CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END)) {
      writer.writeAll(comments, false);
      consumer.accept(writer);
    }
    // no response needed as writer has already been called
    return null;
  }

  /**
   * Creates the comments of the csv response (Attribution, API-Version and optional Metadata).
   */
  private List<String[]> createCsvTopComments(Metadata metadata) {
    List<String[]> comments = new LinkedList<>();
    comments.add(new String[] {"# Copyright URL: " + extractMetadata.getAttributionUrl()});
    comments.add(new String[] {"# Copyright Text: " + extractMetadata.getAttributionShort()});
    comments.add(new String[] {"# API Version: " + ohsomeAPIVersion});
    if (metadata != null) {
      comments.add(new String[] {"# Execution Time: " + metadata.getExecutionTime()});
      comments.add(new String[] {"# Description: " + metadata.getDescription()});
      if (metadata.getRequestUrl() != null) {
        comments.add(new String[] {"# Request URL: " + metadata.getRequestUrl()});
      }
    }
    return comments;
  }

  /**
   * Creates the csv response for /elements/_/groupBy requests.
   *
   * @param resultSet <code>GroupByObject</code> array containing <code>GroupByResult</code> objects
   *        containing <code>ElementsResult</code> objects
   * @return <code>Pair</code> containing the column names (left) and the data rows (right)
   */
  private ImmutablePair<List<String>, List<String[]>> createCsvResponseForElementsGroupBy(
        GroupByObject[] resultSet) {
    List<String> columnNames = new LinkedList<>();
    columnNames.add("timestamp");
    List<String[]> rows = new LinkedList<>();
    for (int i = 0; i < resultSet.length; i++) {
      GroupByResult groupByResult = (GroupByResult) resultSet[i];
      Object groupByObject = groupByResult.getGroupByObject();
      if (groupByObject instanceof Object[]) {
        Object[] groupByObjectArr = (Object[]) groupByObject;
        columnNames.add(groupByObjectArr[0].toString() + "_" + groupByObjectArr[1].toString());
      } else {
        columnNames.add(groupByObject.toString());
      }
      for (int j = 0; j < groupByResult.getResult().size(); j++) {
        ElementsResult elemResult = (ElementsResult) groupByResult.getResult().get(j);
        if (i == 0) {
          String[] row = new String[resultSet.length + 1];
          row[0] = elemResult.getTimestamp();
          row[1] = String.valueOf(elemResult.getValue());
          rows.add(row);
        } else {
          rows.get(j)[i + 1] = String.valueOf(elemResult.getValue());
        }
      }
    }
    return new ImmutablePair<>(columnNames, rows);
  }

  /**
   * Creates the csv response for /elements/_/ratio/groupBy requests.
   *
   * @param resultSet <code>GroupByObject</code> array containing <code>RatioGroupByResult</code>
   *        objects containing <code>RatioResult</code> objects
   * @return <code>Pair</code> containing the column names (left) and the data rows (right)
   */
  private static ImmutablePair<List<String>, List<String[]>>
  createCsvResponseForElementsRatioGroupBy(GroupByObject[] resultSet) {
    List<String> columnNames = new LinkedList<>();
    columnNames.add("timestamp");
    List<String[]> rows = new LinkedList<>();
    for (int i = 0; i < resultSet.length; i++) {
      RatioGroupByResult ratioGroupByResult = (RatioGroupByResult) resultSet[i];
      columnNames.add(ratioGroupByResult.getGroupByObject() + "_value");
      columnNames.add(ratioGroupByResult.getGroupByObject() + "_value2");
      columnNames.add(ratioGroupByResult.getGroupByObject() + "_ratio");
      for (int j = 0; j < ratioGroupByResult.getRatioResult().length; j++) {
        RatioResult ratioResult = ratioGroupByResult.getRatioResult()[j];
        if (i == 0) {
          String[] row = new String[resultSet.length * 3 + 1];
          row[0] = ratioResult.getTimestamp();
          row[1] = String.valueOf(ratioResult.getValue());
          row[2] = String.valueOf(ratioResult.getValue2());
          row[3] = String.valueOf(ratioResult.getRatio());
          rows.add(row);
        } else {
          int count = i * 3 + 1;
          rows.get(j)[count] = String.valueOf(ratioResult.getValue());
          rows.get(j)[count + 1] = String.valueOf(ratioResult.getValue2());
          rows.get(j)[count + 2] = String.valueOf(ratioResult.getRatio());
        }
      }
    }
    return new ImmutablePair<>(columnNames, rows);
  }

  /**
   * Creates the csv response for /users/_/groupBy requests.
   *
   * @param resultSet <code>GroupByObject</code> array containing <code>GroupByResult</code> objects
   *        containing <code>ContributionsResult</code> objects
   * @return <code>Pair</code> containing the column names (left) and the data rows (right)
   */
  private static ImmutablePair<List<String>, List<String[]>> createCsvResponseForUsersGroupBy(
      GroupByObject[] resultSet) {
    List<String> columnNames = new LinkedList<>();
    columnNames.add("fromTimestamp");
    columnNames.add("toTimestamp");
    List<String[]> rows = new LinkedList<>();
    for (int i = 0; i < resultSet.length; i++) {
      GroupByResult groupByResult = (GroupByResult) resultSet[i];
      columnNames.add(groupByResult.getGroupByObject().toString());
      for (int j = 0; j < groupByResult.getResult().size(); j++) {
        ContributionsResult contributionsResult =
            (ContributionsResult) groupByResult.getResult().get(j);
        if (i == 0) {
          String[] row = new String[resultSet.length + 2];
          row[0] = contributionsResult.getFromTimestamp();
          row[1] = contributionsResult.getToTimestamp();
          row[2] = String.valueOf(contributionsResult.getValue());
          rows.add(row);
        } else {
          int count = i + 2;
          rows.get(j)[count] = String.valueOf(contributionsResult.getValue());
        }
      }
    }
    return new ImmutablePair<>(columnNames, rows);
  }

  /**
   * Creates a new CSVWriter, writes the given comments and returns the writer object.
   *
   * @throws IOException thrown by {@link javax.servlet.ServletResponse#getWriter() getWriter}
   */
  private static CSVWriter writeComments(
      HttpServletResponse servletResponse, List<String[]> comments) throws IOException {
    CSVWriter writer =
        new CSVWriter(servletResponse.getWriter(), ';', CSVWriter.DEFAULT_QUOTE_CHARACTER,
            CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END);
    writer.writeAll(comments, false);
    return writer;
  }
}