package models;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="images")
public class Images {
  @Id
  String id;

  private String region;
  private String description;
  private boolean defunct;

  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }

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
  public boolean isDefunct() {
    return defunct;
  }
  public void setDefunct(boolean defunct) {
    this.defunct = defunct;
  }

}
