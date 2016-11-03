package models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

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
  @Column(name="vpc_id")
  private String vpcId;
  private boolean defunct;
  
  @ManyToOne
  @JoinColumn(name="user_id")
  @JsonIgnoreProperties({"securityGroups"})
  private User instructor;

  public User getInstructor() {
    return instructor;
  }
  public void setInstructor(User instructor) {
    this.instructor = instructor;
  }
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
  public String getVpcId() {
    return vpcId;
  }
  public void setVpcId(String vpcId) {
    this.vpcId = vpcId;
  }
  public boolean isDefunct() {
    return defunct;
  }
  public void setDefunct(boolean defunct) {
    this.defunct = defunct;
  }
}
