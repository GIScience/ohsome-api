package org.heigit.bigspatialdata.ohsome.springBootWebAPI.controller;

import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;

import org.heigit.bigspatialdata.ohsome.springBootWebAPI.content.input.AggregationContent;
import org.heigit.bigspatialdata.ohsome.springBootWebAPI.content.output.MetaData;
import org.heigit.bigspatialdata.ohsome.springBootWebAPI.content.output.dataAggregationResponse.ElementsResponseContent;
import org.heigit.bigspatialdata.ohsome.springBootWebAPI.content.output.dataAggregationResponse.Result;
import org.heigit.bigspatialdata.ohsome.springBootWebAPI.exception.NotImplementedException;
import org.heigit.bigspatialdata.ohsome.springBootWebAPI.inputValidation.InputValidator;
import org.heigit.bigspatialdata.oshdb.api.generic.lambdas.SerializableFunction;
import org.heigit.bigspatialdata.oshdb.api.mapreducer.MapReducer;
import org.heigit.bigspatialdata.oshdb.api.objects.OSMEntitySnapshot;
import org.heigit.bigspatialdata.oshdb.api.utils.OSHDBTimestamp;
import org.heigit.bigspatialdata.oshdb.util.Geo;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vividsolutions.jts.geom.Geometry;

/**
 * REST controller containing the methods to handle GET and POST requests, which
 * enter through "/elements".
 *
 */
@RestController
@RequestMapping("/elements")
public class ElementsController {

	// (default) values
	private final String defBox = "abc";
	private final String defUser = "664409"; // username = cmh_germany (random user in Heidelberg)
	private final String defType = "type";
	private final String defKey = "key";
	private final String defVal = "val";
	// represents the latest timestamp, where the latest data is available
	private final String defTime = "2017-11-01";

	// HD: 8.6528, 49.3683, 8.7294, 49.4376

	/*
	 * GET Requests start here
	 */

	/**
	 * Gets the amount of OSM objects, which fit to the given parameters.
	 * <p>
	 * 
	 * @param bbox
	 *            <code>String</code> array containing lon1, lat1, lon2, lat2
	 *            values, which have to be <code>double</code> parse-able. If bbox
	 *            is given, bpoint and bpoly must be <code>null</code> or
	 *            <code>empty</code>. If neither of these parameters is given, a
	 *            global request is computed.
	 * @param bpoint
	 *            <code>String</code> array containing lon, lat, radius values,
	 *            which have to be <code>double</code> parse-able. If bpoint is
	 *            given, bbox and bpoly must be <code>null</code> or
	 *            <code>empty</code>.
	 * @param bpoly
	 *            <code>String</code> array containing lon1, lat1, ..., lonN, latN
	 *            values, which have to be <code>double</code> parse-able. If bpoly
	 *            is given, bbox and bpoint must be <code>null</code> or
	 *            <code>empty</code>.
	 * @param types
	 *            <code>String</code> array containing one or more strings defining
	 *            the OSMType. It can be "node" and/or "way" and/or "relation". If
	 *            <code>null</code> or <code>empty</code>, all types are used.
	 * @param keys
	 *            <code>String</code> array containing one or more keys.
	 * @param values
	 *            <code>String</code> array containing one or more values. Must be
	 *            less or equal than <code>keys.length()</code> anf values[n] must
	 *            pair with keys[n].
	 * @param users
	 *            <code>String</code> array containing one or more user-IDs.
	 * @param time
	 *            <code>String</code> array that holds a list of timestamps or a
	 *            datetimestring, which fits to one of the formats used by the
	 *            method
	 *            {@link org.heigit.bigspatialdata.ohsome.springBootWebAPI.inputValidation.InputValidator#extractTime(String)
	 *            extractTime(String time)}.
	 * 
	 * @return {@link org.heigit.bigspatialdata.ohsome.springBootWebAPI.content.output.dataAggregationResponse.ElementsResponseContent
	 *         ElementsResponseContent} object containing the count of results for
	 *         the given request as JSON response as well as additional info.
	 * @throws UnsupportedOperationException
	 *             thrown by
	 *             {@link org.heigit.bigspatialdata.oshdb.api.mapreducer.MapReducer#aggregateByTimestamp()
	 *             aggregateByTimestamp()}
	 * @throws Exception
	 *             thrown by
	 *             {@link org.heigit.bigspatialdata.oshdb.api.mapreducer.MapAggregator#count()
	 *             count()}
	 */
	@RequestMapping("/count")
	public ElementsResponseContent getCount(@RequestParam(value = "bbox", defaultValue = defBox) String[] bbox,
			@RequestParam(value = "bpoint", defaultValue = defBox) String[] bpoint,
			@RequestParam(value = "bpoly", defaultValue = defBox) String[] bpoly,
			@RequestParam(value = "types", defaultValue = defType) String[] types,
			@RequestParam(value = "keys", defaultValue = defKey) String[] keys,
			@RequestParam(value = "values", defaultValue = defVal) String[] values,
			@RequestParam(value = "users", defaultValue = defUser) String[] users,
			@RequestParam(value = "time", defaultValue = defTime) String[] time)
			throws UnsupportedOperationException, Exception {

		long startTime = System.currentTimeMillis();
		SortedMap<OSHDBTimestamp, Integer> result;
		MapReducer<OSMEntitySnapshot> mapRed;
		InputValidator iV = new InputValidator();
		// input parameter processing
		mapRed = iV.processParameters(true, null, bbox, bpoint, bpoly, types, keys, values, users, time);
		// db result
		result = mapRed.aggregateByTimestamp().count();
		// output
		Result[] resultSet = new Result[result.size()];
		int count = 0;
		for (Entry<OSHDBTimestamp, Integer> entry : result.entrySet()) {
			resultSet[count] = new Result(entry.getKey().formatIsoDateTime(),
					String.valueOf(entry.getValue().intValue()));
			count++;
		}
		long duration = System.currentTimeMillis() - startTime;
		// response
		ElementsResponseContent response = new ElementsResponseContent(
				"Lorem ipsum dolor sit amet, consetetur sadipscing elitr,",
				"sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua.",
				new MetaData(duration, "amount",
						"Total number of items (elements, tags, changesets or contributors) related to the elements selected by the parameters."),
				resultSet);
		return response;
	}

	/**
	 * Gets the length of the OSM objects, which fit to the given parameters.
	 * <p>
	 * For description of the parameters, <code>return</code> object and exceptions,
	 * look at the
	 * {@link org.heigit.bigspatialdata.ohsome.springBootWebAPI.controller.ElementsController#getCount(String[], String[], String[], String[], String[], String[], String[], String[])
	 * getCount} method.
	 */
	@RequestMapping("/length")
	public ElementsResponseContent getLength(@RequestParam(value = "bbox", defaultValue = defBox) String[] bbox,
			@RequestParam(value = "bpoint", defaultValue = defBox) String[] bpoint,
			@RequestParam(value = "bpoly", defaultValue = defBox) String[] bpoly,
			@RequestParam(value = "types", defaultValue = defType) String[] types,
			@RequestParam(value = "keys", defaultValue = defKey) String[] keys,
			@RequestParam(value = "values", defaultValue = defVal) String[] values,
			@RequestParam(value = "users", defaultValue = defUser) String[] users,
			@RequestParam(value = "time", defaultValue = defTime) String[] time)
			throws UnsupportedOperationException, Exception {

		long startTime = System.currentTimeMillis();
		SortedMap<OSHDBTimestamp, Number> result;
		MapReducer<OSMEntitySnapshot> mapRed;
		InputValidator iV = new InputValidator();
		// input parameter processing
		mapRed = iV.processParameters(true, null, bbox, bpoint, bpoly, types, keys, values, users, time);
		// db result
		result = mapRed.aggregateByTimestamp().sum((SerializableFunction<OSMEntitySnapshot, Number>) snapshot -> {
			return Geo.lengthOf(snapshot.getGeometry());
		});
		// output
		Result[] resultSet = new Result[result.size()];
		int count = 0;
		for (Map.Entry<OSHDBTimestamp, Number> entry : result.entrySet()) {
			resultSet[count] = new Result(entry.getKey().formatIsoDateTime(),
					String.valueOf(entry.getValue().floatValue()));
			count++;
		}
		long duration = System.currentTimeMillis() - startTime;
		// response
		ElementsResponseContent response = new ElementsResponseContent("-Hier könnte Ihre Lizenz stehen.-",
				"-Hier könnte Ihr Copyright stehen.-",
				new MetaData(duration, "meter", "Total length of lines and polygon boundaries."), resultSet);
		return response;
	}

	/**
	 * Gets the area of the OSM objects, which fit to the given parameters.
	 * <p>
	 * For description of the parameters, <code>return</code> object and exceptions,
	 * look at the
	 * {@link org.heigit.bigspatialdata.ohsome.springBootWebAPI.controller.ElementsController#getCount(String[], String[], String[], String[], String[], String[], String[], String[])
	 * getCount} method.
	 */
	@RequestMapping("/area")
	public ElementsResponseContent getArea(@RequestParam(value = "bbox", defaultValue = defBox) String[] bbox,
			@RequestParam(value = "bpoint", defaultValue = defBox) String[] bpoint,
			@RequestParam(value = "bpoly", defaultValue = defBox) String[] bpoly,
			@RequestParam(value = "types", defaultValue = defType) String[] types,
			@RequestParam(value = "keys", defaultValue = defKey) String[] keys,
			@RequestParam(value = "values", defaultValue = defVal) String[] values,
			@RequestParam(value = "users", defaultValue = defUser) String[] users,
			@RequestParam(value = "time", defaultValue = defTime) String[] time)
			throws UnsupportedOperationException, Exception {

		long startTime = System.currentTimeMillis();
		SortedMap<OSHDBTimestamp, Number> result;
		MapReducer<OSMEntitySnapshot> mapRed;
		String unit = "square-meters";
		boolean isRelation = false;
		InputValidator iV = new InputValidator();
		// input parameter processing
		mapRed = iV.processParameters(true, null, bbox, bpoint, bpoly, types, keys, values, users, time);
		// db result
		result = mapRed.aggregateByTimestamp().sum((SerializableFunction<OSMEntitySnapshot, Number>) snapshot -> {
			return Geo.areaOf(snapshot.getGeometry());
		});
		// check for relation type
		for (String type : types) {
			if (type.equals("relation")) {
				unit = "square-kilometers";
				isRelation = true;
			}
		}
		// output
		Result[] resultSet = new Result[result.size()];
		int count = 0;
		for (Map.Entry<OSHDBTimestamp, Number> entry : result.entrySet()) {
			if (isRelation)
				resultSet[count] = new Result(entry.getKey().formatIsoDateTime(),
						String.valueOf(entry.getValue().floatValue() / 1000000));
			else
				resultSet[count] = new Result(entry.getKey().formatIsoDateTime(),
						String.valueOf(entry.getValue().floatValue()));
			count++;
		}
		long duration = System.currentTimeMillis() - startTime;
		// response
		ElementsResponseContent response = new ElementsResponseContent("-Hier könnte Ihre Lizenz stehen.-",
				"-Hier könnte Ihr Copyright stehen.-", new MetaData(duration, unit, "Total area of polygons."),
				resultSet);
		return response;
	}

	/**
	 * Gets the mean minimum distance of items to a given bounding point. This
	 * method is not implemented yet.
	 * <p>
	 * For description of the parameters, <code>return</code> object and exceptions,
	 * look at the
	 * {@link org.heigit.bigspatialdata.ohsome.springBootWebAPI.controller.ElementsController#getCount(String[], String[], String[], String[], String[], String[], String[], String[])
	 * getCount} method.
	 */
	@RequestMapping("/mean-minimal-distance")
	public ElementsResponseContent getMeanMinimalDistance(
			@RequestParam(value = "bbox", defaultValue = defBox) String[] bbox,
			@RequestParam(value = "bpoint", defaultValue = defBox) String[] bpoint,
			@RequestParam(value = "bpoly", defaultValue = defBox) String[] bpoly,
			@RequestParam(value = "types", defaultValue = defType) String[] types,
			@RequestParam(value = "keys", defaultValue = defKey) String[] keys,
			@RequestParam(value = "values", defaultValue = defVal) String[] values,
			@RequestParam(value = "users", defaultValue = defUser) String[] users,
			@RequestParam(value = "time", defaultValue = defTime) String[] time)
			throws UnsupportedOperationException, Exception {

		throw new NotImplementedException("This method is not implemented yet.");
	}

	/**
	 * Gets the density of selected items (number of items per area).
	 * <p>
	 * For description of the parameters, <code>return</code> object and exceptions,
	 * look at the
	 * {@link org.heigit.bigspatialdata.ohsome.springBootWebAPI.controller.ElementsController#getCount(String[], String[], String[], String[], String[], String[], String[], String[])
	 * getCount} method.
	 */
	@RequestMapping("/density")
	public ElementsResponseContent getDensity(@RequestParam(value = "bbox", defaultValue = defBox) String[] bbox,
			@RequestParam(value = "bpoint", defaultValue = defBox) String[] bpoint,
			@RequestParam(value = "bpoly", defaultValue = defBox) String[] bpoly,
			@RequestParam(value = "types", defaultValue = defType) String[] types,
			@RequestParam(value = "keys", defaultValue = defKey) String[] keys,
			@RequestParam(value = "values", defaultValue = defVal) String[] values,
			@RequestParam(value = "users", defaultValue = defUser) String[] users,
			@RequestParam(value = "time", defaultValue = defTime) String[] time)
			throws UnsupportedOperationException, Exception {

		long startTime = System.currentTimeMillis();
		SortedMap<OSHDBTimestamp, Integer> countResult;
		MapReducer<OSMEntitySnapshot> mapRed;
		InputValidator iV = new InputValidator();
		// input parameter processing
		mapRed = iV.processParameters(true, null, bbox, bpoint, bpoly, types, keys, values, users, time);
		// count result
		countResult = mapRed.aggregateByTimestamp().count();
		int count = 0;
		Result[] countResultSet = new Result[countResult.size()];
		for (Entry<OSHDBTimestamp, Integer> entry : countResult.entrySet()) {
			countResultSet[count] = new Result(entry.getKey().formatIsoDateTime(),
					String.valueOf(entry.getValue().intValue()));
			count++;
		}
		// geometry
		Geometry geom = null;
		switch (iV.getBoundary()) {
		case 0:
			geom = iV.getBbox().getGeometry();
			break;
		case 1:
			geom = iV.getBbox().getGeometry();
			break;
		case 2:
			geom = iV.getBpoint();
			break;
		case 3:
			geom = iV.getBpoly();
			break;
		}
		// output
		Result[] resultSet = new Result[countResult.size()];
		for (int i = 0; i < resultSet.length; i++) {
			// gets the timestamp and the results from count and divides it through the area
			String date = countResultSet[i].getTimestamp();
			String value = String
					.valueOf(Float.parseFloat(countResultSet[i].getValue()) / (Geo.areaOf(geom) / 1000000));
			resultSet[i] = new Result(date, value);
		}
		long duration = System.currentTimeMillis() - startTime;
		// response
		ElementsResponseContent response = new ElementsResponseContent(
				"-Hier könnte Ihre Lizenz stehen.-", "-Hier könnte Ihr Copyright stehen.-", new MetaData(duration,
						"items per square-kilometer", "Density of selected items (number of items per area)."),
				resultSet);
		return response;
	}

	/**
	 * Gets the ratio of selected items satisfying types2, keys2 and values2 within
	 * items selected by types, keys and values.
	 * 
	 * <p>
	 * For description of the parameters, <code>return</code> object and exceptions,
	 * look at the
	 * {@link org.heigit.bigspatialdata.ohsome.springBootWebAPI.controller.ElementsController#getCount(String[], String[], String[], String[], String[], String[], String[], String[])
	 * getCount} method.
	 */
	@RequestMapping("/ratio")
	public ElementsResponseContent getRatio(@RequestParam(value = "bbox", defaultValue = defBox) String[] bbox,
			@RequestParam(value = "bpoint", defaultValue = defBox) String[] bpoint,
			@RequestParam(value = "bpoly", defaultValue = defBox) String[] bpoly,
			@RequestParam(value = "types", defaultValue = defType) String[] types,
			@RequestParam(value = "keys", defaultValue = defKey) String[] keys,
			@RequestParam(value = "values", defaultValue = defVal) String[] values,
			@RequestParam(value = "users", defaultValue = defUser) String[] users,
			@RequestParam(value = "time", defaultValue = defTime) String[] time,
			@RequestParam(value = "types2", defaultValue = defType) String[] types2,
			@RequestParam(value = "keys2", defaultValue = defType) String[] keys2,
			@RequestParam(value = "values2", defaultValue = defType) String[] values2)
			throws UnsupportedOperationException, Exception {

		long startTime = System.currentTimeMillis();
		SortedMap<OSHDBTimestamp, Integer> result1;
		SortedMap<OSHDBTimestamp, Integer> result2;
		MapReducer<OSMEntitySnapshot> mapRed1;
		MapReducer<OSMEntitySnapshot> mapRed2;
		InputValidator iV = new InputValidator();
		// input parameter processing 1 and result 1
		mapRed1 = iV.processParameters(true, null, bbox, bpoint, bpoly, types, keys, values, users, time);
		result1 = mapRed1.aggregateByTimestamp().count();
		// input parameter processing 2 and result 2
		mapRed2 = iV.processParameters(true, null, bbox, bpoint, bpoly, types2, keys2, values2, users, time);
		result2 = mapRed2.aggregateByTimestamp().count();
		// resultSet 1
		Result[] resultSet1 = new Result[result1.size()];
		int count = 0;
		for (Entry<OSHDBTimestamp, Integer> entry : result1.entrySet()) {
			resultSet1[count] = new Result(entry.getKey().formatIsoDateTime(),
					String.valueOf(entry.getValue().intValue()));
			count++;
		}
		// output
		Result[] resultSet = new Result[result1.size()];
		count = 0;
		for (Entry<OSHDBTimestamp, Integer> entry : result2.entrySet()) {
			// gets the timestamp and the results from both counts and divides 2 through 1
			String date = resultSet1[count].getTimestamp();
			String value = String
					.valueOf(entry.getValue().floatValue() / Float.parseFloat(resultSet1[count].getValue()));
			resultSet[count] = new Result(date, value);
			count++;
		}
		long duration = System.currentTimeMillis() - startTime;
		// response
		ElementsResponseContent response = new ElementsResponseContent(
				"Lorem ipsum dolor sit amet, consetetur sadipscing elitr,",
				"sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua.",
				new MetaData(duration, "ratio",
						"Ratio of items satisfying types2, keys2, values2 within items selected by types, keys, values."),
				resultSet);
		return response;
	}

	
	/*
	 * POST Requests start here
	 */
	

	/**
	 * POST request returning the count of elements for the given parameters. This
	 * method should only be used if more boundary parameters of one category
	 * (bboxes, bpoints, or bpolys) need to be given, or if the request URL would be
	 * too long for a GET request.
	 * 
	 * @param content
	 *            {@link org.heigit.bigspatialdata.ohsome.springBootWebAPI.content.input.AggregationContent
	 *            AggregationContent} object containing all parameters given by the
	 *            client in the request body in JSON.
	 * @return {@link org.heigit.bigspatialdata.ohsome.springBootWebAPI.content.output.dataAggregationResponse.ElementsResponseContent
	 *         ElementsResponseContent} object containing the count of results for
	 *         the given request as JSON response as well as additional info.
	 * @throws UnsupportedOperationException
	 *             thrown by
	 *             {@link org.heigit.bigspatialdata.oshdb.api.mapreducer.MapReducer#aggregateByTimestamp()
	 *             aggregateByTimestamp()}
	 * @throws Exception
	 *             thrown by
	 *             {@link org.heigit.bigspatialdata.oshdb.api.mapreducer.MapAggregator#count()
	 *             count()}
	 */
	@RequestMapping(value = "/count", method = RequestMethod.POST)
	public ElementsResponseContent postCount(@RequestBody AggregationContent content)
			throws UnsupportedOperationException, Exception {

		throw new NotImplementedException("This method is not implemented yet.");

		/*
		 * 
		 * long startTime = System.currentTimeMillis(); SortedMap<OSHDBTimestamp,
		 * Integer> result; MapReducer<OSMEntitySnapshot> mapRed = null; // input
		 * parameter processing //mapRed = processParameters(false, content.getBboxes(),
		 * null, null, null, content.getTypes(), content.getKeys(), content.getValues(),
		 * content.getUsers(), content.getTime()); // db result result =
		 * mapRed.aggregateByTimestamp().count();
		 * 
		 * // output Result[] resultSet = new Result[result.size()]; int count = 0; for
		 * (Entry<OSHDBTimestamp, Integer> entry : result.entrySet()) { resultSet[count]
		 * = new Result(entry.getKey().formatDate(),
		 * String.valueOf(entry.getValue().intValue())); count++; } long duration =
		 * System.currentTimeMillis() - startTime; // response ElementsResponseContent
		 * response = new ElementsResponseContent("-Hier könnte Ihre Lizenz stehen.-",
		 * "-Hier könnte Ihr Copyright stehen.-", new MetaData(duration, "amount"),
		 * resultSet); return response;
		 */
	}

	/**
	 * POST request returning the size of the area for the given parameters. This
	 * method is not implemented yet.
	 * 
	 * @param content
	 * @return
	 * @throws UnsupportedOperationException
	 * @throws Exception
	 */
	@RequestMapping(value = "/area", method = RequestMethod.POST)
	public Number postArea(@RequestBody AggregationContent content) throws UnsupportedOperationException, Exception {

		throw new NotImplementedException("This method is not implemented yet.");
	}

	
}
