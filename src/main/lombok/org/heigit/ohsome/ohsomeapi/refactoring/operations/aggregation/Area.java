package org.heigit.ohsome.ohsomeapi.refactoring.operations.aggregation;

import java.text.DecimalFormat;
import java.util.SortedMap;
import org.heigit.ohsome.ohsomeapi.Application;
import org.heigit.ohsome.ohsomeapi.executor.ExecutionUtils;
import org.heigit.ohsome.ohsomeapi.inputprocessing.InputProcessor;
import org.heigit.ohsome.ohsomeapi.output.Attribution;
import org.heigit.ohsome.ohsomeapi.output.DefaultAggregationResponse;
import org.heigit.ohsome.ohsomeapi.output.Description;
import org.heigit.ohsome.ohsomeapi.output.Metadata;
import org.heigit.ohsome.ohsomeapi.output.elements.ElementsResult;
import org.heigit.ohsome.ohsomeapi.refactoring.operations.Operation;
import org.heigit.ohsome.ohsomeapi.refactoring.results.ElementsResultRefactoring;
import org.heigit.ohsome.ohsomeapi.utilities.MetadataUtility;
import org.heigit.ohsome.oshdb.OSHDBTimestamp;
import org.heigit.ohsome.oshdb.api.mapreducer.MapReducer;
import org.heigit.ohsome.oshdb.util.geometry.Geo;
import org.heigit.ohsome.oshdb.util.mappable.OSMEntitySnapshot;
import org.locationtech.jts.geom.Geometry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Area implements Operation {

  @Autowired
  Attribution attribution;
  public static final DecimalFormat df = ExecutionUtils.defineDecimalFormat("#.##");
  @Autowired
  InputProcessor inputProcessor;
  @Autowired
  ElementsResultRefactoring elementsResultRefactoring;
  @Autowired
  MetadataUtility metadataUtility;

  @Override
  public DefaultAggregationResponse compute() throws Exception {
    MapReducer<OSMEntitySnapshot> mapRed = inputProcessor.processParameters(null);
    var mapRedGeom = mapRed.map(OSMEntitySnapshot::getGeometry);
    final SortedMap<OSHDBTimestamp, ? extends Number> result;
    result = mapRedGeom.aggregateByTimestamp()
        .sum(geom -> ExecutionUtils.cacheInUserData(geom, () -> Geo.areaOf(geom)));
    Geometry geom = inputProcessor.getGeometry();
    //RequestParameters requestParameters = inputProcessor.getProcessingData().getRequestParameters();
    ElementsResult[] resultSet =
        elementsResultRefactoring.fillElementsResult(result, inputProcessor.isDensity(), df, geom);
    String description = Description.aggregate(inputProcessor.isDensity(),
        getDescription(), getUnit());
    Metadata metadata = metadataUtility.generateMetadata(description);
//    if ("csv".equalsIgnoreCase(servletRequest.getParameter("format"))) {
//      return writeCsv(createCsvTopComments(metadata), writeCsvResponse(resultSet));
//    }
    return DefaultAggregationResponse.of(Application.API_VERSION, metadata, resultSet);
    //return result;
  }

  public String getDescription() {
    return "area";
  }

  public String getUnit() {
    return "square meters";
  }
}
