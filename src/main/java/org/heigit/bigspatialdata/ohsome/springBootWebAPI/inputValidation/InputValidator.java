package org.heigit.bigspatialdata.ohsome.springBootWebAPI.inputValidation;

import java.util.ArrayList;
import java.util.EnumSet;

import org.heigit.bigspatialdata.ohsome.springBootWebAPI.exception.BadRequestException;
import org.heigit.bigspatialdata.oshdb.osm.OSMType;
import org.heigit.bigspatialdata.oshdb.util.BoundingBox;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

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
	// represents the latest/earliest timestamp in the OSM history data
	private final String defEndTime = "2017-11-01";
	private final String defStartTime = "2006-11-01";
	private EnumSet<OSMType> osmTypes;

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
		if (bbox[0].equals("abc") && bpoint[0].equals("abc") && bpoly[0].equals("abc"))
			return 0;
		else if (bbox.length == 4 && bpoint.length == 1 && bpoly.length == 1)
			return 1;
		else if (bbox.length == 1 && bpoint.length == 3 && bpoly.length == 1)
			return 2;
		else if (bbox.length == 1 && bpoint.length == 1 && bpoly.length >= 6)
			return 3;
		else
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
		if (bbox.length == 1 && bbox[0].equals("abc"))
			return new BoundingBox(defMinLon, defMaxLon, defMinLat, defMaxLat);
		// the number of elements in the bBoxes array should be 4
		else if (bbox.length == 4) {
			try {
				// parsing of the first bBox
				double minLon = Double.parseDouble(bbox[0]);
				double minLat = Double.parseDouble(bbox[1]);
				double maxLon = Double.parseDouble(bbox[2]);
				double maxLat = Double.parseDouble(bbox[3]);

				return new BoundingBox(minLon, maxLon, minLat, maxLat);

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
		try {
			// creates a point from the coordinates and a buffer
			Point p = geomFact
					.createPoint(new Coordinate(Double.parseDouble(bpoint[0]), Double.parseDouble(bpoint[1])));
			buffer = p.buffer(Double.parseDouble(bpoint[2]));
		} catch (NumberFormatException e) {
			throw new BadRequestException(
					"The bBoxes array must contain double-parseable String values in the order of lon/lat coordinate pairs.");
		}

		return buffer;
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
		try {
			// walks through the string array and parses the coordinates
			for (int i = 0; i < bpoly.length; i += 2) {
				coords.add(new Coordinate(Double.parseDouble(bpoly[i]), Double.parseDouble(bpoly[i + 1])));
			}
			// adds the first coordinate pair again so the polygon will be closed
			coords.add(new Coordinate(Double.parseDouble(bpoly[0]), Double.parseDouble(bpoly[1])));
		} catch (NumberFormatException e) {
			throw new BadRequestException(
					"The bBoxes array must contain double-parseable String values in the order of lon/lat coordinate pairs.");
		}
		// creates a polygon from the coordinates
		Polygon poly = geomFact.createPolygon((Coordinate[]) coords.toArray(new Coordinate[] {}));

		return poly;
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
			throw new BadRequestException("Array containing the OSM Types cannot have more than 3 entries.");
		}

		// check if the types array only contains the default value
		if (types[0].equalsIgnoreCase("type")) {
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
	 * Method to check the content of the keys and values arrays.
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

		if (keys[0].equals("key")) {
			// refers to the default key value "key"
			if (!values[0].equals("val")) {
				// happens when no key, but one (or more) value parameter is given
				throw new BadRequestException(
						"No value parameter can be provided, if there is no corresponding key parameter given.");
			}
		}
		if (keys.length < values.length) {
			// happens when more values than keys are given
			throw new BadRequestException(
					"There cannot be more values than keys. For each value, the respective key has to be provided on the same spot in the array.");
		}

		return true;
	}

	/**
	 * Checks content of the users String array.
	 * 
	 * @param users
	 *            String array containing the IDs of the requested users (must be
	 *            valid whole-number IDs).
	 */
	public void checkUsers(String[] users) {
		for (String user : users) {
			try {
				// tries to parse the String to a long
				Long.valueOf(user);

			} catch (NumberFormatException e) {
				throw new BadRequestException("The 'users' array must contain whole number(s) as ID for OSM user(s).");
			}
		}
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
}
