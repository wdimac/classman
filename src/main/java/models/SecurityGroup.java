package models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="security_groups")
public class SecurityGroup {
  @Id
  private String id;
  private String region;
  @Column(name="owner_id")
  private String ownerId;
  private String name;
  private String description;

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
  public String getOwnerId() {
    return ownerId;
  }
  public void setOwnerId(String ownerId) {
    this.ownerId = ownerId;
  }
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }
  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }
}
