package org.heigit.bigspatialdata.ohsome.springBootWebAPI.content.input;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Represents the parameters, which can be received via data aggregation POST
 * requests.
 * <ul>
 * <li>bboxes:
 * {@link org.heigit.bigspatialdata.ohsome.springBootWebAPI.content.input.BBox
 * BBox} array containing the IDs and the corresponding lon/lat coordinate pairs
 * for the bounding boxes.
 * <li>bpoints:
 * {@link org.heigit.bigspatialdata.ohsome.springBootWebAPI.content.input.BPoint
 * BPoint} array containing the IDs and the corresponding lon/lat coordinate
 * pairs for the bounding points.
 * <li>bpolys:
 * {@link org.heigit.bigspatialdata.ohsome.springBootWebAPI.content.input.BPoly
 * BPoly} array containing the IDs and the corresponding lon/lat coordinate
 * pairs for the bounding polygons.
 * <li>types: <code>String</code> array containing one or more strings defining
 * the OSMType. It can be "node" and/or "way" and/or "relation". If
 * <code>null</code> or <code>empty</code>, all types are used.
 * <li>keys: <code>String</code> array containing one or more keys.
 * <li>values:<code>String</code> array containing one or more values. Must be
 * less or equal than <code>keys.length()</code> and values[n] must pair with
 * keys[n].
 * <li>userids: <code>String</code> array containing one or more user-IDs.
 * <li>time: <code>String</code> array that holds a list of ISO 8601 conform timestamps or a
 * datetimestring, which fits to one of the formats used by the method
 * {@link org.heigit.bigspatialdata.ohsome.springBootWebAPI.inputValidation.InputValidator#extractTime(String)
 * extractTime(String time)}.
 * </ul>
 *
 */
@JsonInclude(Include.NON_NULL) // needed to exclude NULL objects from the result
public class AggregationContent {

	private BBox[] bboxes;
	private BPoint[] bpoints;
	private BPoly[] bpolys;
	private String[] types;
	private String[] keys;
	private String[] values;
	private String[] userids;
	private String[] time;

	public AggregationContent(BBox[] bboxes, BPoint[] bpoints, BPoly[] bpolys, String[] types, String[] keys,
			String[] values, String[] userids, String[] time) {
		this.bboxes = bboxes;
		this.bpoints = bpoints;
		this.bpolys = bpolys;
		this.types = types;
		this.keys = keys;
		this.values = values;
		this.userids = userids;
		this.time = time;
	}

	/**
	 * Empty dummy constructor (needed for Jackson).
	 */
	public AggregationContent() {
	}

	public BBox[] getBboxes() {
		return bboxes;
	}

	public BPoint[] getBpoints() {
		return bpoints;
	}

	public BPoly[] getBpolys() {
		return bpolys;
	}

	public String[] getTypes() {
		return types;
	}

	public String[] getKeys() {
		return keys;
	}

	public String[] getValues() {
		return values;
	}

	public String[] getUserids() {
		return userids;
	}

	public String[] getTime() {
		return time;
	}

}
