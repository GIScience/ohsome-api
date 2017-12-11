package org.heigit.bigspatialdata.ohsome.springBootWebAPI.content.output.dataExtractionResponse;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Third level object in the POST JSON response.
 * This was implemented before the detailed concept of the REST API was defined in Confluence.
 *
 */
@JsonInclude(Include.NON_NULL) // needed to exclude NULL objects from the result
public class OshdbObjects {
	
	private Node node;
	private Way way;
	private Relation relation;
	
	public OshdbObjects(Node node, Way way, Relation relation) {
		this.node = node;
		this.way = way;
		this.relation = relation;
	}
	
	public OshdbObjects(Node node, Way way) {
		this.node = node;
		this.way = way;
	}
	
	public OshdbObjects(Node node, Relation relation) {
		this.node = node;
		this.relation = relation;
	}
	
	public OshdbObjects(Way way, Relation relation) {
		this.way = way;
		this.relation = relation;
	}
	
	public OshdbObjects(Node node) {
		this.node = node;
	}
	
	public OshdbObjects(Way way) {
		this.way = way;
	}
	
	public OshdbObjects(Relation relation) {
		this.relation = relation;
	}
	
	// getters

	public Node getNode() {
		return node;
	}

	public Way getWay() {
		return way;
	}

	public Relation getRelation() {
		return relation;
	}
}
