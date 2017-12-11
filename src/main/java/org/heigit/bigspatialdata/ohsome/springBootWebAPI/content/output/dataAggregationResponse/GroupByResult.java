package org.heigit.bigspatialdata.ohsome.springBootWebAPI.content.output.dataAggregationResponse;

/**
 * Contains the result JSON objects + their respective groupeBy parameter. This
 * class is only used in groupBy requests.
 *
 */
public class GroupByResult {

	private String groupByObj;
	private Result[] result;

	public GroupByResult(String groupByObj, Result[] result) {
		this.groupByObj = groupByObj;
		this.result = result;
	}

	public String getGroupByObj() {
		return groupByObj;
	}

	public Result[] getResult() {
		return result;
	}
}
