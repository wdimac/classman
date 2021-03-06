package models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name="class_type_detail")
public class ClassTypeDetail {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  Long id;

  private String region;

  @ManyToOne
  @JoinColumn(name="class_type_id", updatable=false)
  @JsonIgnoreProperties({"details"})
  private ClassType classType;

  @Column(name="image_id")
  private String imageId;

  @Column(name="instance_type")
  private String instanceType;

  @ManyToOne
  @JoinColumn(name="subnet_id")
  private Vpc subnet;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getRegion() {
    return region;
  }

  public void setRegion(String region) {
    this.region = region;
  }

  public String getImageId() {
    return imageId;
  }

  public void setImageId(String imageId) {
    this.imageId = imageId;
  }

  public String getInstanceType() {
    return instanceType;
  }

  public void setInstanceType(String instanceType) {
    this.instanceType = instanceType;
  }

  public ClassType getClassType() {
    return classType;
  }

  public void setClassType(ClassType classType) {
    this.classType = classType;
  }

  public Vpc getSubnet() {
    return subnet;
  }

  public void setSubnet(Vpc subnet) {
    this.subnet = subnet;
  }
}
