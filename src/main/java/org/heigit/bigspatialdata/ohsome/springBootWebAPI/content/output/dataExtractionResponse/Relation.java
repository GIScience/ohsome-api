package org.heigit.bigspatialdata.ohsome.springBootWebAPI.content.output.dataExtractionResponse;

/**
 * Content of the relation object in the POST JSON response. This was implemented before the
 * detailed concept of the REST API was defined in Confluence.
 *
 */
public class Relation {

  private long id;
  private int userId;
  private int version;

  /**
   * @param id The ID of this object.
   * @param userId The ID of the user who was the last one to edit it.
   * @param version The current version number of this object.
   */
  public Relation(long id, int userId, int version) {
    this.id = id;
    this.userId = userId;
    this.version = version;
  }

  public long getId() {
    return id;
  }

  public int getUserId() {
    return userId;
  }

  public int getVersion() {
    return version;
  }
}
