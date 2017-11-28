package org.heigit.bigspatialdata.ohsome.springBootWebAPI.content.input;

/**
 * Represents the parameters, which can be received via POST requests for the data extraction.
 * This was implemented before the detailed concept of the REST API was defined in Confluence.
 * @author kowatsch
 *
 */
public class SnapshotContent {

	private String begin;
	private String end;
	private String interval;
	private double minLon;
	private double maxLon;
	private double minLat;
	private double maxLat;
	private String[] keys;
	private String[] values;

	/**
	 * @param begin
	 * @param end
	 * @param interval
	 * @param minLon
	 * @param maxLon
	 * @param minLat
	 * @param maxLat
	 */
	public SnapshotContent(String begin, String end, String interval, double minLon, double maxLon, double minLat,
			double maxLat, String[] keys, String[] values) {
		this.begin = begin;
		this.end = end;
		this.interval = interval;
		this.minLon = minLon;
		this.maxLon = maxLon;
		this.minLat = minLat;
		this.maxLat = maxLat;
		this.keys = keys;
		this.values = values;
	}

	/**
	 * empty dummy constructor for Jackson
	 */
	public SnapshotContent() {
	}

	public String getBegin() {
		return begin;
	}

	public String getEnd() {
		return end;
	}

	public String getInterval() {
		return interval;
	}

	public double getMinLon() {
		return minLon;
	}

	public double getMaxLon() {
		return maxLon;
	}

	public double getMinLat() {
		return minLat;
	}

	public double getMaxLat() {
		return maxLat;
	}
	
	public String[] getKeys() {
		return keys;
	}
	
	public String[] getValues() {
		return values;
	}
}
