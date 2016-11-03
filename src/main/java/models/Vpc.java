package models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="vpc")
public class Vpc {
  @Id
  @Column(name="subnet_id")
  private String subnetId;
  @Column(name="vpc_id")
  private String vpcId;
  private String region;
  private boolean defunct;

  public String getVpcId() {
    return vpcId;
  }
  public void setVpcId(String vpcId) {
    this.vpcId = vpcId;
  }
  public String getSubnetId() {
    return subnetId;
  }
  public void setSubnetId(String subnetId) {
    this.subnetId = subnetId;
  }
  public String getRegion() {
    return region;
  }
  public void setRegion(String region) {
    this.region = region;
  }
  public boolean isDefunct() {
    return defunct;
  }
  public void setDefunct(boolean defunct) {
    this.defunct = defunct;
  }

}
