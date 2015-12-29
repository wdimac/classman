package models;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Images {
  @Id
  String id;

  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }

  private String region;
  private String description;

  public String getRegion() {
    return region;
  }

  public void setRegion(String region) {
    this.region = region;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

}
