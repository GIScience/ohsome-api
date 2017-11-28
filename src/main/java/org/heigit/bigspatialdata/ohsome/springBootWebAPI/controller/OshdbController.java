package org.heigit.bigspatialdata.ohsome.springBootWebAPI.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import org.heigit.bigspatialdata.ohsome.springBootWebAPI.content.input.SnapshotContent;
import org.heigit.bigspatialdata.ohsome.springBootWebAPI.content.output.MetaData;
import org.heigit.bigspatialdata.ohsome.springBootWebAPI.content.output.dataExtractionResponse.DataExtractionResponseContent;
import org.heigit.bigspatialdata.ohsome.springBootWebAPI.content.output.dataExtractionResponse.Node;
import org.heigit.bigspatialdata.ohsome.springBootWebAPI.content.output.dataExtractionResponse.OshdbObjects;
import org.heigit.bigspatialdata.ohsome.springBootWebAPI.content.output.dataExtractionResponse.OshdbResult;
import org.heigit.bigspatialdata.ohsome.springBootWebAPI.content.output.dataExtractionResponse.Relation;
import org.heigit.bigspatialdata.ohsome.springBootWebAPI.content.output.dataExtractionResponse.Way;
import org.heigit.bigspatialdata.oshdb.api.db.OSHDB_H2;
import org.heigit.bigspatialdata.oshdb.api.mapreducer.OSMEntitySnapshotView;
import org.heigit.bigspatialdata.oshdb.api.utils.OSHDBTimestamp;
import org.heigit.bigspatialdata.oshdb.api.utils.OSHDBTimestamps;
import org.heigit.bigspatialdata.oshdb.api.utils.OSHDBTimestamps.Interval;
import org.heigit.bigspatialdata.oshdb.api.objects.OSMEntitySnapshot;
import org.heigit.bigspatialdata.oshdb.osm.OSMType;
import org.heigit.bigspatialdata.oshdb.util.BoundingBox;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Spring boot controller, which works with the data extraction requests.
 * This was implemented before the concept of the REST API was defined in Confluence.
 * So it is not up-to-date.
 *
 */
@RestController
public class OshdbController {

	// default values
	private final String defMinLon = "8.7128";
	private final String defMaxLon = "8.7294";
	private final String defMinLat = "49.4183";
	private final String defMaxLat = "49.4376";
	private final String defStart = "2016-01-01";
	private final String defEnd = "2017-01-01";
	private final String defInterval = "yearly";
	// HD: 8.6528, 8.7294, 49.3683, 49.4376
	
	// timer
	long startTime;
	long finishTime;

	private Interval oshdbInterval;

	/**
	 * @param minLon
	 * @param maxLon
	 * @param minLat
	 * @param maxLat
	 * @param begin
	 * @param end
	 * @param interval
	 * @return
	 * @throws UnsupportedOperationException
	 * @throws Exception
	 */
	@RequestMapping("/snapshot")
	public DataExtractionResponseContent getSnapshot(@RequestParam(value = "minLon", defaultValue = defMinLon) double minLon,
			@RequestParam(value = "maxLon", defaultValue = defMaxLon) double maxLon,
			@RequestParam(value = "minLat", defaultValue = defMinLat) double minLat,
			@RequestParam(value = "maxLat", defaultValue = defMaxLat) double maxLat,
			@RequestParam(value = "beginTime", defaultValue = defStart) String begin,
			@RequestParam(value = "endTime", defaultValue = defEnd) String end,
			@RequestParam(value = "interval", defaultValue = defInterval) String interval)
			throws UnsupportedOperationException, Exception {
		// to monitor execution time
		startTime = System.currentTimeMillis();

		// input check
		switch (interval) {
		case "daylie":
			oshdbInterval = OSHDBTimestamps.Interval.DAILY;
			break;
		case "monthly":
			oshdbInterval = OSHDBTimestamps.Interval.MONTHLY;
			break;
		case "yearly":
			oshdbInterval = OSHDBTimestamps.Interval.YEARLY;
			break;
		default:
			oshdbInterval = OSHDBTimestamps.Interval.YEARLY;
		}

		// database
		OSHDB_H2 oshdb = (new OSHDB_H2("C:/Users/kowatsch/Desktop/HeiGIT/oshdb/data/baden-wuerttemberg.oshdb"))
				.multithreading(true);
		OSHDB_H2 oshdbKeytables = new OSHDB_H2("C:/Users/kowatsch/Desktop/HeiGIT/oshdb/data/keytables");

		// query
		SortedMap<OSHDBTimestamp, List<OSMEntitySnapshot>> result = OSMEntitySnapshotView.on(oshdb).keytables(oshdbKeytables)
				.areaOfInterest(new BoundingBox(minLon, maxLon, minLat, maxLat))
				.timestamps(begin, end, oshdbInterval)
				.osmTypes(OSMType.RELATION)
				.where("landuse")
				//.where("highway", "residential")
				.aggregateByTimestamp()
				.collect();

		// output
		long objId;
		int userId;
		int version;
		ArrayList<OshdbResult> resultSet = new ArrayList<OshdbResult>();

		for (Map.Entry<OSHDBTimestamp, List<OSMEntitySnapshot>> entry : result.entrySet()) {
			// output holding all OSM objects for each timestamp
			ArrayList<OshdbObjects> timestampSet = new ArrayList<OshdbObjects>();
			
			for (int i = 0; i < entry.getValue().size(); i++) {
				// extracts the data of each object in the list
				objId = entry.getValue().get(i).getEntity().getId();
				userId = entry.getValue().get(i).getEntity().getUserId();
				version = entry.getValue().get(i).getEntity().getVersion();
				// checks what type of OSM object the current one is (node, way, relation)
				switch(entry.getValue().get(i).getEntity().getType().toString()) {
				case "NODE":
					timestampSet.add(new OshdbObjects(new Node(objId, userId, version)));
					break;
				case "WAY":
					timestampSet.add(new OshdbObjects(new Way(objId, userId, version)));
					break;
				case "RELATION":
					timestampSet.add(new OshdbObjects(new Relation(objId, userId, version)));
					break;
				}
			}
			
			resultSet.add(new OshdbResult(entry.getKey().formatDate(), timestampSet));
		}
		finishTime = System.currentTimeMillis();
		return new DataExtractionResponseContent("abc", new MetaData((finishTime - startTime), "a"), resultSet);
	}
	
	/**
	 * @param content
	 * @return
	 * @throws UnsupportedOperationException
	 * @throws Exception
	 */
	@RequestMapping(value = "/snapshots", method = RequestMethod.POST)
	public DataExtractionResponseContent postSnapshot(
			@RequestBody SnapshotContent content)
			throws UnsupportedOperationException, Exception {
		// to monitor execution time
		startTime = System.currentTimeMillis();

		// input check
		switch (content.getInterval()) {
		case "daylie":
			oshdbInterval = OSHDBTimestamps.Interval.DAILY;
			break;
		case "monthly":
			oshdbInterval = OSHDBTimestamps.Interval.MONTHLY;
			break;
		case "yearly":
			oshdbInterval = OSHDBTimestamps.Interval.YEARLY;
			break;
		default:
			oshdbInterval = OSHDBTimestamps.Interval.YEARLY;
		}

		// database
		OSHDB_H2 oshdb = (new OSHDB_H2("C:/Users/kowatsch/Desktop/HeiGIT/oshdb/data/baden-wuerttemberg.oshdb"))
				.multithreading(true);
		OSHDB_H2 oshdbKeytables = new OSHDB_H2("C:/Users/kowatsch/Desktop/HeiGIT/oshdb/data/keytables");

		// query
		SortedMap<OSHDBTimestamp, List<OSMEntitySnapshot>> result = OSMEntitySnapshotView.on(oshdb).keytables(oshdbKeytables)
				.areaOfInterest(new BoundingBox(content.getMinLon(), content.getMaxLon(), content.getMinLat(), content.getMaxLat()))
				.timestamps(content.getBegin(), content.getEnd(), oshdbInterval)
				.osmTypes(OSMType.NODE, OSMType.WAY)
				.where(content.getKeys()[0])
				.aggregateByTimestamp()
				.collect();

		// output
		long objId;
		int userId;
		int version;
		ArrayList<OshdbResult> resultSet = new ArrayList<OshdbResult>();

		for (Map.Entry<OSHDBTimestamp, List<OSMEntitySnapshot>> entry : result.entrySet()) {
			// output holding all OSM objects for each timestamp
			ArrayList<OshdbObjects> timestampSet = new ArrayList<OshdbObjects>();
			
			for (int i = 0; i < entry.getValue().size(); i++) {
				// extracts data of each object in the list
				objId = entry.getValue().get(i).getEntity().getId();
				userId = entry.getValue().get(i).getEntity().getUserId();
				version = entry.getValue().get(i).getEntity().getVersion();
				// checks what type of OSM object the current one is (node, way, relation)
				switch(entry.getValue().get(i).getEntity().getType().toString()) {
				case "NODE":
					timestampSet.add(new OshdbObjects(new Node(objId, userId, version)));
					break;
				case "WAY":
					timestampSet.add(new OshdbObjects(new Way(objId, userId, version)));
					break;
				case "RELATION":
					timestampSet.add(new OshdbObjects(new Relation(objId, userId, version)));
					break;
				}
			}
			
			resultSet.add(new OshdbResult(entry.getKey().formatDate(), timestampSet));
		}
		finishTime = System.currentTimeMillis();
		return new DataExtractionResponseContent("abc", new MetaData((finishTime - startTime), "a"), resultSet);
	}

	/**
	 * @param id
	 * @param minLon
	 * @param maxLon
	 * @param minLat
	 * @param maxLat
	 * @param begin
	 * @param end
	 * @param interval
	 * @return
	 * @throws UnsupportedOperationException
	 * @throws Exception
	 */
	@RequestMapping("/node")
	public ArrayList<Node> node(@RequestParam(value = "id") String id,
			@RequestParam(value = "minLon", defaultValue = defMinLon) double minLon,
			@RequestParam(value = "maxLon", defaultValue = defMaxLon) double maxLon,
			@RequestParam(value = "minLat", defaultValue = defMinLat) double minLat,
			@RequestParam(value = "maxLat", defaultValue = defMaxLat) double maxLat,
			@RequestParam(value = "beginTime", defaultValue = defStart) String begin,
			@RequestParam(value = "endTime", defaultValue = defEnd) String end,
			@RequestParam(value = "interval", defaultValue = defInterval) String interval) throws UnsupportedOperationException, Exception {
		
		final long idL = Long.parseLong(id);

		// database
		OSHDB_H2 oshdb = (new OSHDB_H2("C:/Users/kowatsch/Desktop/HeiGIT/oshdb/data/baden-wuerttemberg.oshdb"))
				.multithreading(true);
		OSHDB_H2 oshdbKeytables = new OSHDB_H2("C:/Users/kowatsch/Desktop/HeiGIT/oshdb/data/keytables");

		// query
		SortedMap<OSHDBTimestamp, List<OSMEntitySnapshot>> result = OSMEntitySnapshotView.on(oshdb).keytables(oshdbKeytables)
				.areaOfInterest(new BoundingBox(minLon, maxLon, minLat, maxLat))
				.timestamps(begin, end, oshdbInterval)
				.osmTypes(OSMType.NODE)
				.where(entity -> entity.getId() ==  idL)
				.aggregateByTimestamp()
				.collect();

		// output
		long nodeId;
		int userId;
		int version;
		ArrayList<Node> resultSet = new ArrayList<Node>();

		for (Map.Entry<OSHDBTimestamp, List<OSMEntitySnapshot>> entry : result.entrySet()) {
			nodeId = entry.getValue().get(0).getEntity().getId();
			userId = entry.getValue().get(0).getEntity().getUserId();
			version = entry.getValue().get(0).getEntity().getVersion();
			resultSet.add(new Node(nodeId, userId, version));
		}
		return resultSet;
	}

}