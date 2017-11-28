package org.heigit.bigspatialdata.ohsome.springBootWebAPI.content.output.dataAggregationResponse;

import org.heigit.bigspatialdata.ohsome.springBootWebAPI.content.output.MetaData;

/**
 * First level response content for the data aggregation requests. It contains the requested
 * data as well as information about the license, the copyright and additional
 * meta data.
 *
 */
public class ElementsResponseContent {

	private String license;
	private String copyright;
	private MetaData metaData;
	private Result[] response;

	public ElementsResponseContent(String license, String copyright, MetaData metaData, Result[] response) {
		this.license = license;
		this.copyright = copyright;
		this.metaData = metaData;
		this.response = response;
	}

	public String getLicense() {
		return license;
	}

	public String getCopyright() {
		return copyright;
	}

	public MetaData getMetaData() {
		return metaData;
	}

	public Result[] getResponse() {
		return response;
	}
}
