package org.heigit.bigspatialdata.ohsome.springBootWebAPI.inputValidation;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.heigit.bigspatialdata.ohsome.springBootWebAPI.Application;
import org.heigit.bigspatialdata.ohsome.springBootWebAPI.eventHolder.EventHolderBean;
import org.heigit.bigspatialdata.ohsome.springBootWebAPI.exception.BadRequestException;
import org.heigit.bigspatialdata.oshdb.api.db.OSHDB_H2;
import org.heigit.bigspatialdata.oshdb.api.mapreducer.MapReducer;
import org.heigit.bigspatialdata.oshdb.api.mapreducer.OSMEntitySnapshotView;
import org.heigit.bigspatialdata.oshdb.api.objects.OSMEntitySnapshot;
import org.heigit.bigspatialdata.oshdb.api.utils.OSHDBTimestamps;
import org.heigit.bigspatialdata.oshdb.osm.OSMType;
import org.heigit.bigspatialdata.oshdb.util.BoundingBox;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.Polygonal;

/**
 * Holds general validation methods and validates specific parameters given by a
 * GET request. Throws exceptions depending on their validity.
 * 
 * @author kowatsch
 *
 */
public class InputValidator {

	// world: -179.9999, 180, -85.0511, 85.0511
	// default bBox defining the whole area (here: BW)
	private final double defMinLon = 7.3949;
	private final double defMaxLon = 10.6139;
	private final double defMinLat = 47.3937;
	private final double defMaxLat = 49.9079;
	private byte boundary;
	private BoundingBox bbox;
	private Geometry bpoint;
	private Polygon bpoly;
	private final String defVal = "";
	// represents the latest and earliest timestamps
	private final String defEndTime = new SimpleDateFormat("yyyy-MM-dd").format(new Date(System.currentTimeMillis()));
	private final String defStartTime = "2007-11-01";
	private String[] timeData;
	private EnumSet<OSMType> osmTypes;
	/**
	 * [0]:oshdb [1]:keytables
	 */
	private OSHDB_H2[] dbConnObjects;

	/**
	 * default constructor
	 */
	public InputValidator() {

	}

	/**
	 * Checks which boundary parameter is given.
	 * 
	 * @param bbox
	 * @param bpoint
	 * @param bpoly
	 * @return Byte defining which parameter is given: 0 (none is given), 1 (bbox is
	 *         given), 2 (bpoint is given), or 3 (bpoly is given).
	 * @throws BadRequestException
	 *             The provided boundary parameter does not fit to its format, or
	 *             more than one boundary parameter is given.
	 */
	public byte checkBoundaryGet(String[] bbox, String[] bpoint, String[] bpoly) {
		// checks the given parameters
		if (bbox.length==0 && bpoint.length == 0 && bpoly.length == 0) {
			this.boundary = 0;
			return this.boundary;
		} else if (bbox.length == 4 && bpoint.length == 0 && bpoly.length == 0) {
			this.boundary = 1;
			return this.boundary;
		} else if (bbox.length == 0 && bpoint.length == 3 && bpoly.length == 0) {
			this.boundary = 2;
			return this.boundary;
		} else if (bbox.length == 0 && bpoint.length == 0 && bpoly.length >= 6) {
			this.boundary = 3;
			return this.boundary;
		} else
			throw new BadRequestException(
					"Your provided boundary parameter (bbox, bpoint, or bpoly) does not fit its format, "
							+ "or you defined more than one boundary parameter.");
	}

	/**
	 * Creates a <code>BoundingBox</code> object out of the content of the given
	 * <code>String</code> array. This method is used for GET requests only as it
	 * cannot handle more than one bounding box.
	 * 
	 * @param bbox
	 *            <code>String</code> array containing the lon/lat coordinates of
	 *            the bounding box.
	 * @throws BadRequestException
	 *             Invalid coordinates.
	 */
	public BoundingBox createBBoxes(String[] bbox) throws BadRequestException {
		// no bBox given -> global request
		if (bbox.length==0) {
			this.bbox = new BoundingBox(defMinLon, defMaxLon, defMinLat, defMaxLat);
			return this.bbox;
			// the number of elements in the bBoxes array should be 4
		} else if (bbox.length == 4) {
			try {
				// parsing of the first bBox
				double minLon = Double.parseDouble(bbox[0]);
				double minLat = Double.parseDouble(bbox[1]);
				double maxLon = Double.parseDouble(bbox[2]);
				double maxLat = Double.parseDouble(bbox[3]);

				this.bbox = new BoundingBox(minLon, maxLon, minLat, maxLat);

				return this.bbox;

			} catch (NumberFormatException e) {
				throw new BadRequestException(
						"The bBoxes array must contain 4 double-parseable String values in the following order: minLon, minLat, maxLon, maxLat.");
			}
		} else
			throw new BadRequestException(
					"The bBoxes array must contain 4 double-parseable String values in the following order: minLon, minLat, maxLon, maxLat.");
	}

	/**
	 * Creates a <code>Geometry</code> object representing a circle out of the
	 * content of the given <code>String</code> array. This method is used for GET
	 * requests only as it cannot handle more than one bounding point.
	 * 
	 * @param bpoint
	 *            <code>String</code> array containing the lon/lat coordinates of
	 *            the point at [0] and [1] + the size of the buffer at [2].
	 * @return
	 */
	public Geometry createBPoint(String[] bpoint) {
		GeometryFactory geomFact = new GeometryFactory();
		Geometry buffer;
		CoordinateReferenceSystem sourceCRS;
		CoordinateReferenceSystem targetCRS;
		MathTransform transform = null;

		try {
			// Set source and target CRS + transformation
			sourceCRS = CRS.decode("EPSG:4326", true);
			targetCRS = CRS.decode(findEPSG(Double.parseDouble(bpoint[0]), Double.parseDouble(bpoint[1])), true);
			transform = CRS.findMathTransform(sourceCRS, targetCRS, false);

			// creates a point from the coordinates and a buffer
			Point p = geomFact
					.createPoint(new Coordinate(Double.parseDouble(bpoint[0]), Double.parseDouble(bpoint[1])));
			buffer = JTS.transform(p, transform).buffer(Double.parseDouble(bpoint[2]));
			// transform back again
			transform = CRS.findMathTransform(targetCRS, sourceCRS, false);
			this.bpoint = JTS.transform(buffer, transform);
		} catch (FactoryException e) {
			e.printStackTrace();
		} catch (MismatchedDimensionException e) {
			e.printStackTrace();
		} catch (TransformException e) {
			e.printStackTrace();
			throw new BadRequestException(
					"The bpoint array must contain two double-parseable String values in the order of "
							+ "lon/lat coordinate pairs at [0] and [1] as well as a buffer length in meters.");
		}

		return this.bpoint;
	}

	/**
	 * Creates a polygon out of the coordinates in the given array.
	 * 
	 * @param bpoly
	 *            <code>String</code> array containing the lon/lat coordinates of
	 *            the bounding polygon.
	 * @return
	 */
	public Polygon createBPoly(String[] bpoly) {
		GeometryFactory geomFact = new GeometryFactory();
		ArrayList<Coordinate> coords = new ArrayList<Coordinate>();
		// checks if the first and last coordinate pairs are not the same
		if (!bpoly[0].equals(bpoly[bpoly.length - 2]) || !bpoly[1].equals(bpoly[bpoly.length - 1]))
			throw new BadRequestException(
					"The last coordinate pair of the polygon must have the same values as the first coordinate pair.");
		try {
			// walks through the string array and parses the coordinates
			for (int i = 0; i < bpoly.length; i += 2) {
				coords.add(new Coordinate(Double.parseDouble(bpoly[i]), Double.parseDouble(bpoly[i + 1])));
			}
			// adds the first coordinate pair again so the polygon will be closed
			coords.add(new Coordinate(Double.parseDouble(bpoly[0]), Double.parseDouble(bpoly[1])));
		} catch (NumberFormatException e) {
			throw new BadRequestException(
					"The bpoly array must contain double-parseable String values in the order of lon/lat coordinate pairs.");
		}
		// creates a polygon from the coordinates
		this.bpoly = geomFact.createPolygon((Coordinate[]) coords.toArray(new Coordinate[] {}));

		return this.bpoly;
	}

	/**
	 * Extracts the possible time-parameters from the given datetimestring. One of
	 * the following formats is allowed:
	 * <ul>
	 * <li>timestamp: YYYY-MM-DD</li>
	 * <li>start/end: YYYY-MM-DD/YYYY-MM-DD</li>
	 * <li>start/end/period: YYYY-MM-DD/YYYY-MM-DD/PnYnMnD where [n] refers to the
	 * size of the period and 0 <= n <= 99. Example: P1Y10M15D refers to an interval
	 * of one year, 10 months and 15 days.</li>
	 * <li>#/end: /YYYY-MM-DD</li>
	 * <li>#/end/period: /YYYY-MM-DD/PnYnMnD</li>
	 * <li>start/#: YYYY-MM-DD/</li>
	 * <li>start/#/period: YYYY-MM-DD/PnYnMnD/</li>
	 * <li>#/#: /</li>
	 * <li>#/#/period: /PnYnMnD</li>
	 * </ul>
	 * <p>
	 * Note: "period" does also refer to "interval" and # means defaultStart or
	 * defaultEnd referring to the earliest/latest timestamps.
	 * 
	 * @param time
	 *            String, which refers to one of the nine time-formats.
	 * @return String array containing at [0] the startTime at [1] the endTime and
	 *         at [2] the period.
	 * @throws BadRequestException
	 *             The provided time parameter does not fit to any specified format.
	 */
	public String[] extractTime(String time) {
		// needed variables
		byte timeType = 0;
		String[] timeVals = new String[3];
		String[] timeSplit;

		// to check which time format is applied
		timeType = checkTime(time);

		if (timeType == 1) {
			// timestamp
			timeVals[0] = time;
			timeVals[1] = time;
			timeVals[2] = "P1Y";
		} else if (timeType == 2) {
			// start/end
			timeSplit = time.split("/");
			timeVals[0] = timeSplit[0];
			timeVals[1] = timeSplit[1];

		} else if (timeType == 3) {
			// start/end/period
			timeSplit = time.split("/");
			timeVals[0] = timeSplit[0];
			timeVals[1] = timeSplit[1];
			timeVals[2] = timeSplit[2];

		} else if (timeType == 4) {
			// defStart/end
			timeSplit = time.split("/");
			timeVals[0] = defStartTime;
			timeVals[1] = timeSplit[1];

		} else if (timeType == 5) {
			// defStart/end/period
			timeSplit = time.split("/");
			timeVals[0] = defStartTime;
			timeVals[1] = timeSplit[1];
			timeVals[2] = timeSplit[2];

		} else if (timeType == 6) {
			// start/defEnd
			timeSplit = time.split("/");
			timeVals[0] = timeSplit[0];
			timeVals[1] = defEndTime;

		} else if (timeType == 7) {
			// start/defEnd/period
			timeSplit = time.split("/");
			timeVals[0] = timeSplit[0];
			timeVals[1] = defEndTime;
			timeVals[2] = timeSplit[2];

		} else if (timeType == 8) {
			// defStart/defEnd
			timeVals[0] = defStartTime;
			timeVals[1] = defEndTime;

		} else if (timeType == 9) {
			// defStart/defEnd/period
			timeSplit = time.split("/");
			timeVals[0] = defStartTime;
			timeVals[1] = defEndTime;
			timeVals[2] = timeSplit[2];

		} else {
			throw new BadRequestException("The provided time parameter does not fit to any specified time format.");
		}

		return timeVals;
	}

	/**
	 * Checks and extracts the content of the OSM types array.
	 * 
	 * @param types
	 *            String array containing 1, 2, or all 3 types. If default value is
	 *            given, all 3 types will be used
	 */
	public EnumSet<OSMType> checkTypes(String[] types) {
		// checks if the types array is too big
		if (types.length > 3) {
			throw new BadRequestException("Parameter containing the OSM Types cannot have more than 3 entries.");
		}

		// check if the types array only contains the default value (length == 0)
		if (types.length==0) {
			return EnumSet.of(OSMType.NODE, OSMType.WAY, OSMType.RELATION);
		}

		// complex if-else structure, which adds the corresponding OSMType(s) depending
		// on the String(s) and throws exceptions if they aren't one or more of "node",
		// "way", "relation"
		if (types.length == 1) {
			if (types[0].equalsIgnoreCase("node"))
				osmTypes = EnumSet.of(OSMType.NODE);
			else if (types[0].equalsIgnoreCase("way"))
				osmTypes = EnumSet.of(OSMType.WAY);
			else if (types[0].equalsIgnoreCase("relation"))
				osmTypes = EnumSet.of(OSMType.RELATION);
			else
				throw new BadRequestException(
						"Array containing the OSM-Types can only have one, two, or all three of the following Strings: 'node', 'way', 'relation'.");
		} else if (types.length == 2) {
			if (types[0].equalsIgnoreCase("node")) {
				if (types[1].equalsIgnoreCase("way"))
					osmTypes = EnumSet.of(OSMType.NODE, OSMType.WAY);
				else if (types[1].equalsIgnoreCase("relation"))
					osmTypes = EnumSet.of(OSMType.NODE, OSMType.RELATION);
				else
					throw new BadRequestException(
							"Array containing the OSM-Types can only have one, two, or all three of the following Strings: 'node', 'way', 'relation'.");
			} else if (types[0].equalsIgnoreCase("way")) {
				if (types[1].equalsIgnoreCase("node"))
					osmTypes = EnumSet.of(OSMType.WAY, OSMType.NODE);
				else if (types[1].equalsIgnoreCase("relation"))
					osmTypes = EnumSet.of(OSMType.WAY, OSMType.RELATION);
				else
					throw new BadRequestException(
							"Array containing the OSM-Types can only have one, two, or all three of the following Strings: 'node', 'way', 'relation'.");
			} else if (types[0].equalsIgnoreCase("relation")) {
				if (types[1].equalsIgnoreCase("node"))
					osmTypes = EnumSet.of(OSMType.RELATION, OSMType.NODE);
				else if (types[1].equalsIgnoreCase("way"))
					osmTypes = EnumSet.of(OSMType.RELATION, OSMType.WAY);
				else
					throw new BadRequestException(
							"Array containing the OSM-Types can only have one, two, or all three of the following Strings: 'node', 'way', 'relation'.");
			}
			// happens when array.size == 3
		} else if ((types[0].equalsIgnoreCase("node") && types[1].equalsIgnoreCase("way")
				&& types[2].equalsIgnoreCase("relation"))
				|| (types[0].equalsIgnoreCase("node") && types[1].equalsIgnoreCase("relation")
						&& types[2].equalsIgnoreCase("way"))
				|| (types[0].equalsIgnoreCase("way") && types[1].equalsIgnoreCase("node")
						&& types[2].equalsIgnoreCase("relation"))
				|| (types[0].equalsIgnoreCase("way") && types[1].equalsIgnoreCase("relation")
						&& types[2].equalsIgnoreCase("node"))
				|| (types[0].equalsIgnoreCase("relation") && types[1].equalsIgnoreCase("node")
						&& types[2].equalsIgnoreCase("way"))
				|| (types[0].equalsIgnoreCase("relation") && types[1].equalsIgnoreCase("way")
						&& types[2].equalsIgnoreCase("node")))
			osmTypes = EnumSet.of(OSMType.NODE, OSMType.WAY, OSMType.RELATION);
		else
			throw new BadRequestException(
					"Array containing the OSM-Types can only have one, two, or all three of the following Strings: 'node', 'way', 'relation'.");
		return osmTypes;
	}

	/**
	 * Method to compare the size of the keys and values arrays.
	 * 
	 * @param keys
	 *            String array, which contains the provided key parameters.
	 * @param values
	 *            String array, which contains the provided value parameters. Has to
	 *            be smaller than or equal to the length of the keys array.
	 * 
	 * @throws BadRequestException
	 *             The number of provided values compared to the keys parameter(s)
	 *             is incorrect.
	 */
	public boolean checkKeysValues(String[] keys, String[] values) {

		if (keys.length < values.length) {
			throw new BadRequestException(
					"There cannot be more values than keys. For each value in the values parameter, the respective key has to be provided at the same index in the keys parameter.");
		}

		return true;
	}

	/**
	 * Checks content of the userids String array.
	 * 
	 * @param userids
	 *            String array containing the IDs of the requested userids (must be
	 *            valid whole-number IDs).
	 */
	public void checkuserids(String[] userids) {
		for (String user : userids) {
			try {
				// tries to parse the String to a long
				Long.valueOf(user);

			} catch (NumberFormatException e) {
				throw new BadRequestException(
						"The userids parameter can only contain valid OSM userids, which are always a positive whole number");
			}
		}
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
	 * @param userids
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
	public MapReducer<OSMEntitySnapshot> processParameters(boolean isGet, String[] boundaryParam, String[] bbox,
			String[] bpoint, String[] bpoly, String[] types, String[] keys, String[] values, String[] userids,
			String[] time) {

		// InputValidatorPost iVP = new InputValidatorPost();
		MapReducer<OSMEntitySnapshot> mapRed;

		// database
		EventHolderBean bean = Application.getEventHolderBean();
		dbConnObjects = bean.getDbConnObjects();
		mapRed = OSMEntitySnapshotView.on(dbConnObjects[0]).keytables(dbConnObjects[1]);

		// checks if this method is called for a GET or a POST request
		if (isGet) {
			// boundary (no parameter = 0, bbox = 1, bpoint = 2, or bpoly = 3)
			boundary = checkBoundaryGet(bbox, bpoint, bpoly);
			if (boundary == 0) {
				mapRed = mapRed.areaOfInterest(createBBoxes(bbox));
			} else if (boundary == 1) {
				mapRed = mapRed.areaOfInterest(createBBoxes(bbox));
			} else if (boundary == 2) {
				mapRed = mapRed.areaOfInterest((Geometry & Polygonal) createBPoint(bpoint));
			} else if (boundary == 3) {
				mapRed = mapRed.areaOfInterest(createBPoly(bpoly));
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
		osmTypes = checkTypes(types);
		mapRed = mapRed.osmTypes(osmTypes);

		// time parameter
		if (time.length == 1) {
			timeData = extractTime(time[0]);
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
		checkKeysValues(keys, values);
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

		// checks if the userids parameter is not empty (POST) and does not have the
		// default value (GET)
		if (userids != null && userids.length != 0) {
			checkuserids(userids);
			// more efficient way to include all userIDs
			Set<Integer> useridSet = new HashSet<>();
			for (String user : userids)
				useridSet.add(Integer.valueOf(user));

			mapRed = mapRed.where(entity -> {
				return useridSet.contains(entity.getUserId());
			});
		}

		return mapRed;
	}

	/**
	 * Finds and returns the EPSG code of the given point. Adapted code from
	 * UTMCodeFromLonLat.java class in the osmatrix project.
	 * 
	 * @param lon
	 *            Longitude coordinate of the point.
	 * @param lat
	 *            Latitude coordinate of the point.
	 * @return String representing the corresponding EPSG code.
	 */
	private String findEPSG(double lon, double lat) {

		if (lat >= 84)
			return "EPSG:32661"; // UPS North
		if (lat < -80)
			return "EPSG:32761"; // UPS South

		int zoneNumber = (int) (Math.floor((lon + 180) / 6) + 1);

		if (lat >= 56.0 && lat < 64.0 && lon >= 3.0 && lon < 12.0)
			zoneNumber = 32;

		// Special zones for Svalbard
		if (lat >= 72.0 && lat < 84.0) {
			if (lon >= 0.0 && lon < 9.0)
				zoneNumber = 31;
			else if (lon >= 9.0 && lon < 21.0)
				zoneNumber = 33;
			else if (lon >= 21.0 && lon < 33.0)
				zoneNumber = 35;
			else if (lon >= 33.0 && lon < 42.0)
				zoneNumber = 37;
		}

		String isNorth = (lat > 0) ? "6" : "7";
		String zone = (zoneNumber < 10) ? "0" + zoneNumber : "" + zoneNumber;
		return "EPSG:32" + isNorth + zone;
	}

	/**
	 * Checks the time parameter on its type and returns a respective byte value. #
	 * means here the earliest/latest date available. It works with regular
	 * expressions (RegEx) to extract the time depending on the different formats.
	 * <ul>
	 * <li>timestamp: 1</li>
	 * <li>start/end: 2</li>
	 * <li>start/end/period: 3</li>
	 * <li>#/end: 4</li>
	 * <li>#/end/period: 5</li>
	 * <li>start/#: 6</li>
	 * <li>start/#/period: 7</li>
	 * <li>#/#: 8</li>
	 * <li>#/#/period: 9</li>
	 * <li>invalid: throws BadRequestException</li>
	 * </ul>
	 * 
	 * @param time
	 *            String holding the unparsed time information.
	 * @return Byte number corresponding to the used time format.
	 * @throws BadRequestException
	 *             The provided time parameter does not fit to any specified format.
	 */
	private byte checkTime(String time) {
		byte timeType = 0;

		// regex expressions to differentiate between the 9 possible time formats
		if (time.matches("\\d{4}-\\d{2}-\\d{2}")) {
			timeType = 1;
		} else if (time.matches("\\d{4}-\\d{2}-\\d{2}/\\d{4}-\\d{2}-\\d{2}")) {
			timeType = 2;
		} else if (time.matches("\\d{4}-\\d{2}-\\d{2}/\\d{4}-\\d{2}-\\d{2}/[P]\\d{1}[Y]")
				|| time.matches("\\d{4}-\\d{2}-\\d{2}/\\d{4}-\\d{2}-\\d{2}/[P]\\d{2}[Y]")
				|| time.matches("\\d{4}-\\d{2}-\\d{2}/\\d{4}-\\d{2}-\\d{2}/[P]\\d{1}[M]")
				|| time.matches("\\d{4}-\\d{2}-\\d{2}/\\d{4}-\\d{2}-\\d{2}/[P]\\d{2}[M]")
				|| time.matches("\\d{4}-\\d{2}-\\d{2}/\\d{4}-\\d{2}-\\d{2}/[P]\\d{1}[D]")
				|| time.matches("\\d{4}-\\d{2}-\\d{2}/\\d{4}-\\d{2}-\\d{2}/[P]\\d{2}[D]")) {
			timeType = 3;
		} else if (time.matches("/\\d{4}-\\d{2}-\\d{2}")) {
			timeType = 4;
		} else if (time.matches("/\\d{4}-\\d{2}-\\d{2}/[P]\\d{1}[Y]")
				|| time.matches("/\\d{4}-\\d{2}-\\d{2}/[P]\\d{2}[Y]")
				|| time.matches("/\\d{4}-\\d{2}-\\d{2}/[P]\\d{1}[M]")
				|| time.matches("/\\d{4}-\\d{2}-\\d{2}/[P]\\d{2}[M]")
				|| time.matches("/\\d{4}-\\d{2}-\\d{2}/[P]\\d{1}[D]")
				|| time.matches("/\\d{4}-\\d{2}-\\d{2}/[P]\\d{2}[D]")) {
			timeType = 5;
		} else if (time.matches("\\d{4}-\\d{2}-\\d{2}/")) {
			timeType = 6;
		} else if (time.matches("\\d{4}-\\d{2}-\\d{2}//[P]\\d{1}[Y]")
				|| time.matches("\\d{4}-\\d{2}-\\d{2}//[P]\\d{2}[Y]")
				|| time.matches("\\d{4}-\\d{2}-\\d{2}//[P]\\d{1}[M]")
				|| time.matches("\\d{4}-\\d{2}-\\d{2}//[P]\\d{2}[M]")
				|| time.matches("\\d{4}-\\d{2}-\\d{2}//[P]\\d{1}[D]")
				|| time.matches("\\d{4}-\\d{2}-\\d{2}//[P]\\d{2}[D]")) {
			timeType = 7;
		} else if (time.matches("/")) {
			timeType = 8;
		} else if (time.matches("//[P]\\d{1}[Y]") || time.matches("//[P]\\d{2}[Y]") || time.matches("//[P]\\d{1}[M]")
				|| time.matches("//[P]\\d{2}[M]") || time.matches("//[P]\\d{1}[D]") || time.matches("//[P]\\d{2}[D]")) {
			timeType = 9;
		} else {
			throw new BadRequestException("The provided time parameter does not fit to any specified time format.");
		}

		return timeType;
	}

	public byte getBoundary() {
		return boundary;
	}

	public BoundingBox getBbox() {
		return bbox;
	}

	public Geometry getBpoint() {
		return bpoint;
	}

	public Polygon getBpoly() {
		return bpoly;
	}
}
