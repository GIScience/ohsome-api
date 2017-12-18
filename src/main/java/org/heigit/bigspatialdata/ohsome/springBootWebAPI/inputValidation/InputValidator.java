package org.heigit.bigspatialdata.ohsome.springBootWebAPI.inputValidation;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeParseException;
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
import org.heigit.bigspatialdata.ohsome.springBootWebAPI.exception.NotImplementedException;
import org.heigit.bigspatialdata.oshdb.api.db.OSHDB_H2;
import org.heigit.bigspatialdata.oshdb.api.mapreducer.MapReducer;
import org.heigit.bigspatialdata.oshdb.api.mapreducer.OSMEntitySnapshotView;
import org.heigit.bigspatialdata.oshdb.api.object.OSMEntitySnapshot;
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
 * Holds general input validation and computation methods and validates specific
 * parameters given by the request. Throws exceptions depending on their
 * validity.
 *
 */
public class InputValidator {

	// HD: 8.6528, 49.3683, 8.7294, 49.4376
	// world: -179.9999, 180, -85.0511, 85.0511
	// default bbox defining the whole area (here: BW)
	private final double defMinLon = 7.3949;
	private final double defMaxLon = 10.6139;
	private final double defMinLat = 47.3937;
	private final double defMaxLat = 49.9079;
	private byte boundary;
	private BoundingBox bbox;
	private Geometry bpoint;
	private Polygon bpoly;
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
	 * Method to process the input parameters of a POST or GET request.
	 * 
	 * @param bboxes
	 *            <code>String</code> array containing lon1, lat1, lon2, lat2
	 *            values, which have to be <code>double</code> parse-able. If bboxes
	 *            is given, bpoints and bpolys must be <code>null</code> or
	 *            <code>empty</code>. If neither of these parameters is given, a
	 *            global (=whole dataset) request is computed.
	 * @param bpoints
	 *            <code>String</code> array containing lon, lat, radius values,
	 *            which have to be <code>double</code> parse-able. If bpoints is
	 *            given, bboxes and bpolys must be <code>null</code> or
	 *            <code>empty</code>.
	 * @param bpolys
	 *            <code>String</code> array containing lon1, lat1, ..., lonN, latN
	 *            values, which have to be <code>double</code> parse-able. If bpolys
	 *            is given, bboxes and bpoints must be <code>null</code> or
	 *            <code>empty</code>.
	 * @param types
	 *            <code>String</code> array containing one or more strings defining
	 *            the OSMType. It can be "node" and/or "way" and/or "relation". If
	 *            <code>null</code> or <code>empty</code>, all 3 types are used.
	 * @param keys
	 *            <code>String</code> array containing one or more keys.
	 * @param values
	 *            <code>String</code> array containing one or more values. Must be
	 *            less or equal than <code>keys.length()</code> and values[n] must
	 *            pair with keys[n].
	 * @param userids
	 *            <code>String</code> array containing one or more user-IDs.
	 * @param time
	 *            <code>String</code> array that holds a list of timestamps or a
	 *            datetimestring, which fits to one of the formats used by the
	 *            method
	 *            {@link org.heigit.bigspatialdata.ohsome.springBootWebAPI.inputValidation.InputValidator#extractIsoTime(String)
	 *            extractIsoTime(String time)}.
	 * @return <code>MapReducer<OSMEntitySnapshot></code> object including the
	 *         settings derived from the given parameters.
	 */
	public MapReducer<OSMEntitySnapshot> processParameters(boolean isPost, String[] bboxes, String[] bpoints,
			String[] bpolys, String[] types, String[] keys, String[] values, String[] userids, String[] time) {

		// check if this method is called from a POST request
		if (isPost) {
			// sets the string array to empty if it is null
			bboxes = checkParameterOnNull(bboxes);
			bpoints = checkParameterOnNull(bpoints);
			bpolys = checkParameterOnNull(bpolys);
			types = checkParameterOnNull(types);
			keys = checkParameterOnNull(keys);
			values = checkParameterOnNull(values);
			userids = checkParameterOnNull(userids);
			time = checkParameterOnNull(time);
		}
		MapReducer<OSMEntitySnapshot> mapRed;

		// database
		EventHolderBean bean = Application.getEventHolderBean();
		dbConnObjects = bean.getDbConnObjects();
		mapRed = OSMEntitySnapshotView.on(dbConnObjects[0]).keytables(dbConnObjects[1]);

		// boundary (no parameter = 0, bboxes = 1, bpoints = 2, or bpolys = 3)
		boundary = checkBoundary(bboxes, bpoints, bpolys);
		if (boundary == 0) {
			mapRed = mapRed.areaOfInterest(createBbox(bboxes));
		} else if (boundary == 1) {
			mapRed = mapRed.areaOfInterest((Geometry & Polygonal) createBboxes(bboxes));
		} else if (boundary == 2) {
			mapRed = mapRed.areaOfInterest((Geometry & Polygonal) createCircularPolygon(bpoints));
		} else if (boundary == 3) {
			mapRed = mapRed.areaOfInterest(createbpolys(bpolys));
		} else
			throw new BadRequestException(
					"Your provided boundary parameter (bboxes, bpoints, or bpolys) does not fit its format. "
							+ "or you defined more than one boundary parameter.");

		// osm-type (node, way, relation)
		osmTypes = checkTypes(types);
		mapRed = mapRed.osmTypes(osmTypes);

		// time parameter
		if (time.length == 1) {
			timeData = extractIsoTime(time[0]);
			if (timeData[2] != null) {
				// interval is given
				mapRed = mapRed.timestamps(new OSHDBTimestamps(timeData[0], timeData[1], timeData[2]));
			} else
				// no interval given
				mapRed = mapRed.timestamps(timeData[0], timeData[1]);
		} else if (time.length == 0) {
			// if no time parameter given --> return the default end time
			mapRed = mapRed.timestamps(defEndTime);
		} else {
			// list of timestamps
			String firstElem = time[0];
			time = ArrayUtils.remove(time, 0);
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
			// adds empty entries in the tempVal array
			for (int i = values.length; i < keys.length; i++) {
				tempVal[i] = "";
			}
			values = tempVal;
		}
		// prerequisites: both arrays (keys and values) must be of the same length
		// and key-value pairs need to be at the same index in both arrays
		for (int i = 0; i < keys.length; i++) {
			if (values[i].equals(""))
				mapRed = mapRed.where(keys[i]);
			else
				mapRed = mapRed.where(keys[i], values[i]);
		}

		// checks if the userids parameter is not empty
		if (userids.length != 0) {
			checkUserids(userids);
			// more efficient way to include all userIDs
			Set<Integer> useridSet = new HashSet<>();
			for (String user : userids)
				useridSet.add(Integer.valueOf(user));

			mapRed = mapRed.where(entity -> {
				return useridSet.contains(entity.getUserId());
			});
		} else {
			// do nothing --> all users will be used
		}

		return mapRed;
	}

	/**
	 * Checks which boundary parameter is given.
	 * 
	 * @param bboxes
	 *            <code>String</code> array containing the lon/lat coordinate pairs
	 *            of the bounding boxes.
	 * @param bpoints
	 *            <code>String</code> array containing the lon/lat coordinate pairs
	 *            and the radius of the bounding points.
	 * @param bpolys
	 *            <code>String</code> array containing the lon/lat coordinate pairs
	 *            of the bounding polygons.
	 * @return Byte defining which parameter is given: 0 (none is given), 1 (bboxes
	 *         are given), 2 (bpoints are given), or 3 (bpolys are given).
	 * @throws BadRequestException
	 *             The provided boundary parameter does not fit to its format, or
	 *             more than one boundary parameter is given.
	 */
	private byte checkBoundary(String[] bboxes, String[] bpoints, String[] bpolys) {
		// checks the given parameters
		if ((bboxes.length == 0 || bboxes.length == 4) && bpoints.length == 0 && bpolys.length == 0) {
			this.boundary = 0;
			return this.boundary;
		} else if (bboxes.length > 4 && bpoints.length == 0 && bpolys.length == 0) {
			if (bboxes.length % 4 != 0)
				throw new BadRequestException(
						"Each of your provided bboxes must consist of 2 lon/lat points (bottom left and top right).");
			this.boundary = 1;
			return this.boundary;
		} else if (bboxes.length == 0 && bpoints.length >= 3 && bpolys.length == 0) {
			if (bpoints.length % 3 != 0)
				throw new BadRequestException(
						"Each of your provided bpoints must consist of 1 lon/lat point plus a radius.");
			this.boundary = 2;
			return this.boundary;
		} else if (bboxes.length == 0 && bpoints.length == 0 && bpolys.length >= 6) {
			if (bpolys.length % 2 != 0)
				throw new BadRequestException(
						"Each of your provided bpolys must consist of n lon/lat coordinate pairs.");
			this.boundary = 3;
			return this.boundary;
		} else
			throw new BadRequestException(
					"Your provided boundary parameter (bboxes, bpoints, or bpolys) does not fit its format, "
							+ "or you defined more than one boundary parameter.");
	}

	/**
	 * Creates a <code>BoundingBox</code> object out of the content of the given
	 * <code>String</code> array. Only used if one or no bounding box is given.
	 * 
	 * @param bbox
	 *            <code>String</code> array containing the lon/lat coordinates of
	 *            the bounding box. It must consist of 2 lon/lat coordinate pairs
	 *            (bottom-left and top-right).
	 * 
	 * @return <code>BoundingBox</code> object.
	 * @throws BadRequestException
	 *             Invalid coordinates.
	 */
	private BoundingBox createBbox(String[] bbox) throws BadRequestException {
		if (bbox.length == 0) {
			// no bboxes given -> global request
			this.bbox = new BoundingBox(defMinLon, defMaxLon, defMinLat, defMaxLat);
			return this.bbox;
		} else if (bbox.length == 4) {
			try {
				// parsing of the bbox values
				double minLon = Double.parseDouble(bbox[0]);
				double minLat = Double.parseDouble(bbox[1]);
				double maxLon = Double.parseDouble(bbox[2]);
				double maxLat = Double.parseDouble(bbox[3]);
				// creation of the bbox
				this.bbox = new BoundingBox(minLon, maxLon, minLat, maxLat);
				return this.bbox;
			} catch (NumberFormatException e) {
				throw new BadRequestException(
						"The bounding box must contain double-parseable values in the following order: minLon, minLat, maxLon, maxLat.");
			}
		} else {
			throw new BadRequestException(
					"The bounding box must contain double-parseable values in the following order: minLon, minLat, maxLon, maxLat.");
		}
	}

	/**
	 * Creates a unified <code>Geometry</code> object out of the content of the
	 * given <code>String</code> array. Only used if more than one bounding box is
	 * given in the input array.
	 * 
	 * @param bboxes
	 *            <code>String</code> array containing the lon/lat coordinates of
	 *            the bounding boxes. Each bounding box must consist of 2 lon/lat
	 *            coordinate pairs (bottom-left and top-right).
	 * 
	 * @return <code>Geometry</code> object representing the unified bounding boxes
	 *         as a polygon.
	 * @throws BadRequestException
	 *             Invalid coordinates.
	 */
	private Geometry createBboxes(String[] bboxes) throws BadRequestException {
		try {
			Geometry unifiedBbox;
			GeometryFactory gf = new GeometryFactory();
			// parsing of the first bboxes values
			double minLon = Double.parseDouble(bboxes[0]);
			double minLat = Double.parseDouble(bboxes[1]);
			double maxLon = Double.parseDouble(bboxes[2]);
			double maxLat = Double.parseDouble(bboxes[3]);
			// creation of the first bbox
			this.bbox = new BoundingBox(minLon, maxLon, minLat, maxLat);
			unifiedBbox = gf.createGeometry(this.bbox.getGeometry());

			for (int i = 4; i < bboxes.length; i += 4) {
				// parsing of the other bboxes values
				minLon = Double.parseDouble(bboxes[i]);
				minLat = Double.parseDouble(bboxes[i + 1]);
				maxLon = Double.parseDouble(bboxes[i + 2]);
				maxLat = Double.parseDouble(bboxes[i + 3]);
				this.bbox = new BoundingBox(minLon, maxLon, minLat, maxLat);
				// union of the bboxes
				unifiedBbox = unifiedBbox.union(this.bbox.getGeometry());
			}
			return unifiedBbox;
		} catch (NumberFormatException e) {
			throw new BadRequestException(
					"The bboxeses array must contain double-parseable values in the following order: minLon, minLat, maxLon, maxLat.");
		}
	}

	/**
	 * Creates a <code>Geometry</code> object around the coordinates of the given
	 * <code>String</code> array. This method is used for GET requests only as it
	 * cannot handle more than one bounding point.
	 * 
	 * @param bpoints
	 *            <code>String</code> array containing the lon/lat coordinates of
	 *            the point at [0] and [1] and the size of the buffer at [2].
	 * 
	 * @return <code>Geometry</code> object representing a circular polygon around
	 *         the bounding point.
	 * 
	 * @throws BadRequestException
	 *             Invalid coordinates or radius.
	 */
	private Geometry createCircularPolygon(String[] bpoints) {
		GeometryFactory geomFact = new GeometryFactory();
		Geometry buffer;
		CoordinateReferenceSystem sourceCRS;
		CoordinateReferenceSystem targetCRS;
		MathTransform transform = null;

		try {
			// Set source and target CRS + transformation
			sourceCRS = CRS.decode("EPSG:4326", true);
			targetCRS = CRS.decode(findEPSG(Double.parseDouble(bpoints[0]), Double.parseDouble(bpoints[1])), true);
			transform = CRS.findMathTransform(sourceCRS, targetCRS, false);

			// creates a point from the coordinates and a buffer
			Point p = geomFact
					.createPoint(new Coordinate(Double.parseDouble(bpoints[0]), Double.parseDouble(bpoints[1])));
			buffer = JTS.transform(p, transform).buffer(Double.parseDouble(bpoints[2]));
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
					"The bpoints array must contain two double-parseable String values in the order of "
							+ "lon/lat coordinate pairs at [0] and [1] as well as a buffer length in meters.");
		}

		return this.bpoint;
	}

	/**
	 * Creates a polygon out of the coordinates in the given array. If more polygons
	 * are given, a union of the polygons is created.
	 * 
	 * @param bpolys
	 *            <code>String</code> array containing the lon/lat coordinates of
	 *            the bounding polygon(s).
	 * @return <code>Geometry</code> object representing a circular polygon around
	 *         the bounding point.
	 * 
	 * @throws BadRequestException
	 *             Invalid coordinates.
	 * @throws NotImplementedException
	 *             The processing of more than one polygon is not implemented yet.
	 */
	private Polygon createbpolys(String[] bpolys) throws BadRequestException {
		GeometryFactory geomFact = new GeometryFactory();
		ArrayList<Coordinate> coords = new ArrayList<Coordinate>();

		// checks if the first and last coordinate pairs are the same (= only 1 polygon)
		if (bpolys[0].equals(bpolys[bpolys.length - 2]) && bpolys[1].equals(bpolys[bpolys.length - 1])) {
			try {
				// walks through the string array and parses the coordinates
				for (int i = 0; i < bpolys.length; i += 2) {
					coords.add(new Coordinate(Double.parseDouble(bpolys[i]), Double.parseDouble(bpolys[i + 1])));
				}
				// adds the first coordinate pair again so the polygon will be closed
				coords.add(new Coordinate(Double.parseDouble(bpolys[0]), Double.parseDouble(bpolys[1])));
			} catch (NumberFormatException e) {
				throw new BadRequestException(
						"The bpolys parameter must contain double-parseable values in form of lon/lat coordinate pairs.");
			}
			// creates a polygon from the coordinates
			this.bpoly = geomFact.createPolygon((Coordinate[]) coords.toArray(new Coordinate[] {}));

			return this.bpoly;
		} else {
			throw new NotImplementedException("Being able to process more than one polygon is not implemented yet.");
			// TODO still gives an error at union
			// needs to be worked out in a more complex way
			// see:
			// https://gis.stackexchange.com/questions/71605/combine-several-polygon-objects-in-one-polygon-object-with-geotools-is-it-possi

			/*
			 * Collection<Geometry> geometryCollection = new HashSet<Geometry>(); Coordinate
			 * firstPoint;
			 * 
			 * try { // sets the first point and adds it to the arraylist firstPoint = new
			 * Coordinate(Double.parseDouble(bpolys[0]), Double.parseDouble(bpolys[1]));
			 * coords.add(firstPoint);
			 * 
			 * // walks through all remaining coordinates, creates polygons and adds them to
			 * // the collection for (int i = 2; i < bpolys.length; i += 2) { // compares
			 * the current point to the first point if (firstPoint.x ==
			 * Double.parseDouble(bpolys[i]) && firstPoint.y == Double.parseDouble(bpolys[i
			 * + 1])) { Polygon poly; coords.add(new
			 * Coordinate(Double.parseDouble(bpolys[i]), Double.parseDouble(bpolys[i +
			 * 1]))); // creates a polygon from the coordinates poly =
			 * geomFact.createPolygon((Coordinate[]) coords.toArray(new Coordinate[] {}));
			 * geometryCollection.add(poly);
			 * 
			 * // clear the coords array coords.removeAll(coords); if (i+2 >= bpolys.length)
			 * break; // set the new first point firstPoint.x =
			 * Double.parseDouble(bpolys[i+2]); firstPoint.y =
			 * Double.parseDouble(bpolys[i+3]); // add it to the array
			 * coords.add(firstPoint); i+=2; } else coords.add(new
			 * Coordinate(Double.parseDouble(bpolys[i]), Double.parseDouble(bpolys[i +
			 * 1]))); } // creates a union out of the polygons in the collection for
			 * (Geometry g : geometryCollection) { if (this.bpoly == null) this.bpoly =
			 * (Polygon) g; else { this.bpoly = (Polygon) this.bpoly.union((Polygon) g); }
			 * 
			 * }
			 * 
			 * } catch (NumberFormatException e) { throw new BadRequestException(
			 * "The bpolys parameter must contain double-parseable values in form of lon/lat coordinate pairs."
			 * ); }
			 * 
			 * return this.bpoly;
			 */
		}

	}

	/**
	 * Checks and extracts the content of the types parameter.
	 * 
	 * @param types
	 *            <code>String</code> array containing 1, 2, or all 3 OSM types
	 *            (node, way, relation). If the array is empty, all 3 types will be
	 *            used
	 * 
	 * @return <code>EnumSet</code> containing the requested OSM type(s).
	 * @throws BadRequestException
	 *             If the content of the parameter does not represent one, two, or
	 *             all three OSM types.
	 */
	private EnumSet<OSMType> checkTypes(String[] types) {
		// checks if the types array is too big
		if (types.length > 3) {
			throw new BadRequestException("Parameter containing the OSM Types cannot have more than 3 entries.");
		}
		// check if the types array only contains the default value (length == 0)
		if (types.length == 0) {
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
	 *            <code>String</code> array, which contains the provided key
	 *            parameters.
	 * @param values
	 *            <code>String</code> array, which contains the provided value
	 *            parameters. Has to be smaller than or equal to the length of the
	 *            keys array.
	 * 
	 * @throws BadRequestException
	 *             The number of provided values compared to the keys parameter(s)
	 *             is incorrect.
	 */
	private boolean checkKeysValues(String[] keys, String[] values) {

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
	 * 
	 * @throws BadRequestException
	 *             If one of the userids is invalid.
	 */
	private void checkUserids(String[] userids) {
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
	 * Finds and returns the EPSG code of the given point, which is needed for
	 * {@link org.heigit.bigspatialdata.ohsome.springBootWebAPI.inputValidation.InputValidator#createCircularPolygon
	 * createCircularPolygon}. Adapted code from UTMCodeFromLonLat.java class in the
	 * osmatrix project (by Michael Auer)
	 * 
	 * @param lon
	 *            Longitude coordinate of the point.
	 * @param lat
	 *            Latitude coordinate of the point.
	 * @return <code>String</code> representing the corresponding EPSG code.
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
	 * Extracts the time information out of the time parameter and checks the
	 * content on its format and
	 * <a href="https://en.wikipedia.org/wiki/ISO_8601">ISO-8601</a> conformity.
	 * This method is only used if time.length == 1. Following time formats are
	 * allowed:
	 * <ul>
	 * <li><strong>YYYY-MM-DD</strong> or <strong>YYYY-MM-DDThh:mm:ss</strong>: When
	 * a timestamp includes 'T', hh:mm must also be given. This applies for all time
	 * formats, which use timestamps.</li>
	 * <li><strong>YYYY-MM-DD/YYYY-MM-DD</strong>: start/end timestamps</li>
	 * <li><strong>YYYY-MM-DD/YYYY-MM-DD/PnYnMnD</strong>: start/end/period where n
	 * refers to the size of the respective period</li>
	 * <li><strong>/YYYY-MM-DD</strong>: #/end where # equals the earliest
	 * timestamp</li>
	 * <li><strong>/YYYY-MM-DD/PnYnMnD</strong>: #/end/period</li>
	 * <li><strong>YYYY-MM-DD/</strong>: start/# where # equals the latest
	 * timestamp</li>
	 * <li><strong>YYYY-MM-DD//PnYnMnD</strong>: start/#/period</li>
	 * <li><strong>/</strong>: #/# where # equals the earliest and latest
	 * timestamp</li>
	 * <li><strong>//PnYnMnD</strong>: #/#/period</li>
	 * <li><strong>invalid</strong>: throws BadRequestException</li>
	 * </ul>
	 * <p>
	 * For clarification: the format YYYY-MM-DDThh:mm:ss can be applied to any
	 * format, where a timestamp is used and # is a replacement holder for "no
	 * value". Note that the positioning and using of the forward slash '/' is very
	 * important.
	 * 
	 * @param time
	 *            String holding the unparsed time information.
	 * @return String array containing at [0] the startTime at [1] the endTime and
	 *         at [2] the period.
	 * @throws BadRequestException
	 *             The provided time parameter does not fit to any specified format.
	 */
	private String[] extractIsoTime(String time) {
		String[] timeVals = new String[3];
		if (time.contains("/")) {
			if (time.length() == 1) {
				// only "/" is given
				timeVals[0] = defStartTime;
				timeVals[1] = defEndTime;
				return timeVals;
			}
			String[] timeSplit = time.split("/");
			if (timeSplit[0].length() > 0) {
				// start timestamp
				try {
					if (timeSplit[0].length() == 10) {
						LocalDate.parse(timeSplit[0]);
					} else {
						LocalDateTime.parse(timeSplit[0]);
					}
					timeVals[0] = timeSplit[0];
					if (time.endsWith("/")) {
						// latest timestamp
						timeVals[1] = defEndTime;
						return timeVals;
					}
				} catch (DateTimeParseException e) {
					throw new BadRequestException(
							"The start time of the provided time parameter is not ISO-8601 conform.");
				}
			} else {
				// earliest timestamp
				timeVals[0] = defStartTime;
			}

			if (timeSplit[1].length() > 0) {
				// end timestamp
				try {
					if (timeSplit[1].length() == 10) {
						LocalDate.parse(timeSplit[1]);
					} else {
						LocalDateTime.parse(timeSplit[1]);
					}
					timeVals[1] = timeSplit[1];
				} catch (DateTimeParseException e) {
					throw new BadRequestException(
							"The end time of the provided time parameter is not ISO-8601 conform.");
				}
			} else {
				// latest timestamp
				timeVals[1] = defEndTime;
			}

			if (timeSplit.length == 3 && timeSplit[2].length() > 0) {
				// interval
				try {
					Period.parse(timeSplit[2]);
					timeVals[2] = timeSplit[2];
				} catch (DateTimeParseException e) {
					throw new BadRequestException(
							"The interval (period) of the provided time parameter is not ISO-8601 conform.");
				}
			}

		} else {
			// just one timestamp
			try {
				if (time.length() == 10) {
					LocalDate.parse(time);
				} else {
					LocalDateTime.parse(time);
				}
				timeVals[0] = time;
				timeVals[1] = time;
				timeVals[2] = "P1Y";
			} catch (DateTimeParseException e) {
				throw new BadRequestException("The provided time parameter is not ISO-8601 conform.");
			}
		}

		return timeVals;
	}

	/**
	 * Checking if an input parameter of a POST request is null.
	 * 
	 * @param toCheck
	 *            <code>String</code> array, which is checked.
	 * @return <code>String</code> array, which is empty, but not null.
	 */
	private String[] checkParameterOnNull(String[] toCheck) {
		if (toCheck == null)
			toCheck = new String[0];
		return toCheck;
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
