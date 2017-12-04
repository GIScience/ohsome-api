package org.heigit.bigspatialdata.ohsome.springBootWebAPI.controller;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;

import org.apache.commons.lang3.ArrayUtils;
import org.heigit.bigspatialdata.ohsome.springBootWebAPI.Application;
import org.heigit.bigspatialdata.ohsome.springBootWebAPI.content.input.AggregationContent;
import org.heigit.bigspatialdata.ohsome.springBootWebAPI.content.output.MetaData;
import org.heigit.bigspatialdata.ohsome.springBootWebAPI.content.output.dataAggregationResponse.ElementsResponseContent;
import org.heigit.bigspatialdata.ohsome.springBootWebAPI.content.output.dataAggregationResponse.Result;
import org.heigit.bigspatialdata.ohsome.springBootWebAPI.eventHolder.EventHolderBean;
import org.heigit.bigspatialdata.ohsome.springBootWebAPI.exception.BadRequestException;
import org.heigit.bigspatialdata.ohsome.springBootWebAPI.exception.NotImplementedException;
import org.heigit.bigspatialdata.ohsome.springBootWebAPI.inputValidation.InputValidator;
import org.heigit.bigspatialdata.oshdb.api.db.OSHDB_H2;
import org.heigit.bigspatialdata.oshdb.api.generic.lambdas.SerializableFunction;
import org.heigit.bigspatialdata.oshdb.api.mapreducer.MapReducer;
import org.heigit.bigspatialdata.oshdb.api.mapreducer.OSMEntitySnapshotView;
import org.heigit.bigspatialdata.oshdb.api.objects.OSMEntitySnapshot;
import org.heigit.bigspatialdata.oshdb.api.utils.OSHDBTimestamp;
import org.heigit.bigspatialdata.oshdb.api.utils.OSHDBTimestamps;
import org.heigit.bigspatialdata.oshdb.osm.OSMType;
import org.heigit.bigspatialdata.oshdb.util.BoundingBox;
import org.heigit.bigspatialdata.oshdb.util.Geo;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.Polygonal;

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
	private String[] timeData;
	private BoundingBox bbox;
	private Geometry bpoint;
	private Polygon bpoly;
	private EnumSet<OSMType> osmTypes;
	private byte boundary;

	// HD: 8.6528, 49.3683, 8.7294, 49.4376
	/**
	 * [0]:oshdb [1]:keytables
	 */
	private OSHDB_H2[] dbConnObjects;

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
		// input parameter processing
		mapRed = processParameters(true, null, bbox, bpoint, bpoly, types, keys, values, users, time);
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
		// input parameter processing
		mapRed = processParameters(true, null, bbox, bpoint, bpoly, types, keys, values, users, time);
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
		// input parameter processing
		mapRed = processParameters(true, null, bbox, bpoint, bpoly, types, keys, values, users, time);
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
		// input parameter processing
		mapRed = processParameters(true, null, bbox, bpoint, bpoly, types, keys, values, users, time);
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
		switch (boundary) {
		case 0:
			geom = this.bbox.getGeometry();
			break;
		case 1:
			geom = this.bbox.getGeometry();
			break;
		case 2:
			geom = this.bpoint;
			break;
		case 3:
			geom = this.bpoly;
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
		// input parameter processing 1 and result 1
		mapRed1 = processParameters(true, null, bbox, bpoint, bpoly, types, keys, values, users, time);
		result1 = mapRed1.aggregateByTimestamp().count();
		// input parameter processing 2 and result 2
		mapRed2 = processParameters(true, null, bbox, bpoint, bpoly, types2, keys2, values2, users, time);
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

	/**
	 * Method to process the input parameters of a POST or GET request.
	 * 
	 * @param isGet
	 *            <code>boolean</code> value stating <code>true</code> if this
	 *            method is called from a GET request and <code>false</code> if it
	 *            is called from a POST request.
	 * @param boundaryParam
	 *            <code>String</code> array containing the boundary parameter from a
	 *            POST request. Null in case of a GET request.
	 * @param bbox
	 *            <code>String</code> array containing lon1, lat1, lon2, lat2
	 *            values, which have to be <code>double</code> parse-able. If bbox
	 *            is given, bpoint and bpoly must be <code>null</code> or
	 *            <code>empty</code>. If neither of these parameters is given, a
	 *            global request is computed. Null in case of POST requests.
	 * @param bpoint
	 *            <code>String</code> array containing lon, lat, radius values,
	 *            which have to be <code>double</code> parse-able. If bpoint is
	 *            given, bbox and bpoly must be <code>null</code> or
	 *            <code>empty</code>. Null in case of POST requests.
	 * @param bpoly
	 *            <code>String</code> array containing lon1, lat1, ..., lonN, latN
	 *            values, which have to be <code>double</code> parse-able. If bpoly
	 *            is given, bbox and bpoint must be <code>null</code> or
	 *            <code>empty</code>. Null in case of POST requests.
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
	 * @return <code>MapReducer<OSMEntitySnapshot></code> object including the
	 *         settings derived from the given parameters.
	 */
	private MapReducer<OSMEntitySnapshot> processParameters(boolean isGet, String[] boundaryParam, String[] bbox,
			String[] bpoint, String[] bpoly, String[] types, String[] keys, String[] values, String[] users,
			String[] time) {
		InputValidator iV = new InputValidator();
		// InputValidatorPost iVP = new InputValidatorPost();
		MapReducer<OSMEntitySnapshot> mapRed;

		// database
		EventHolderBean bean = Application.getEventHolderBean();
		dbConnObjects = bean.getDbConnObjects();
		mapRed = OSMEntitySnapshotView.on(dbConnObjects[0]).keytables(dbConnObjects[1]);

		// checks if this method is called for a GET or a POST request
		if (isGet) {
			// boundary (no parameter = 0, bbox = 1, bpoint = 2, or bpoly = 3)
			boundary = iV.checkBoundaryGet(bbox, bpoint, bpoly);
			if (boundary == 0) {
				this.bbox = iV.createBBoxes(bbox);
				mapRed = mapRed.areaOfInterest(this.bbox);
			} else if (boundary == 1) {
				this.bbox = iV.createBBoxes(bbox);
				mapRed = mapRed.areaOfInterest(this.bbox);
			} else if (boundary == 2) {
				this.bpoint = iV.createBPoint(bpoint);
				mapRed = mapRed.areaOfInterest((Geometry & Polygonal) this.bpoint);
			} else if (boundary == 3) {
				this.bpoly = iV.createBPoly(bpoly);
				mapRed = mapRed.areaOfInterest(this.bpoly);
			} else
				throw new BadRequestException(
						"Your provided boundary parameter (bbox, bpoint, or bpoly) does not fit its format. "
								+ "or you defined more than one boundary parameter.");
		} else {
			// TODO implement a checkBoundaryPost method
			// bounding box
			// this.bbox = iVP.checkBBoxes(bboxes);
			// mapRed = mapRed.areaOfInterest(this.bbox);
		}

		// osm-type (node, way, relation)
		osmTypes = iV.checkTypes(types);
		mapRed = mapRed.osmTypes(osmTypes);

		// time parameter
		if (time.length == 1) {
			timeData = iV.extractTime(time[0]);
			if (timeData[2] != null) {
				mapRed = mapRed.timestamps(new OSHDBTimestamps(timeData[0], timeData[1], timeData[2]));
			} else
				mapRed = mapRed.timestamps(timeData[0], timeData[1]);
		} else {
			// gets the first element and removes it from the list
			String firstElem = time[0];
			time = ArrayUtils.remove(time, 0);
			// calls the method to give a list of timestamps
			mapRed = mapRed.timestamps(firstElem, time);
		}

		// key/value parameters
		iV.checkKeysValues(keys, values);
		if (keys.length != values.length) {
			String[] tempVal = new String[keys.length];
			// extracts the value entries from the old values array
			for (int a = 0; a < values.length; a++) {
				tempVal[a] = values[a];
			}
			// adds the defVal to the empty spots in the tempVal array
			for (int i = values.length; i < keys.length; i++) {
				tempVal[i] = defVal;
			}
			values = tempVal;
		}
		// prerequisites: both arrays (keys and values) must be of the same length
		// and key-value pairs need to be at the same index in both arrays
		for (int i = 0; i < keys.length; i++) {
			if (values[i].equals(defVal))
				mapRed = mapRed.where(keys[i]);
			else
				mapRed = mapRed.where(keys[i], values[i]);
		}

		// checks if the users parameter is not empty (POST) and does not have the
		// default value (GET)
		if (users != null && !users[0].equals("664409")) {
			iV.checkUsers(users);
			// more efficient way to include all userIDs
			Set<Integer> userSet = new HashSet<>();
			for (String user : users)
				userSet.add(Integer.valueOf(user));

			mapRed = mapRed.where(entity -> {
				return userSet.contains(entity.getUserId());
			});
		}

		return mapRed;
	}
}
