package models;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name="class_type_detail")
public class ClassTypeDetail {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  Long id;

  private String region;

  @ManyToOne(cascade=CascadeType.REMOVE)
  @JoinColumn(name="class_type_id", updatable=false)
  @JsonBackReference
  private ClassType classType;

  @ManyToOne(cascade=CascadeType.DETACH)
  @JoinColumn(name="image_id")
  private Images image;

  @Column(name="instance_type")
  private String instanceType;

  @ManyToOne(cascade=CascadeType.REMOVE)
  @JoinColumn(name="security_group_id")
  private SecurityGroup securityGroup;

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

  public Images getImage() {
    return image;
  }

  public void setImage(Images image) {
    this.image = image;
  }

  public String getInstanceType() {
    return instanceType;
  }

  public void setInstanceType(String instanceType) {
    this.instanceType = instanceType;
  }

  public SecurityGroup getSecurityGroup() {
    return securityGroup;
  }

  public void setSecurityGroup(SecurityGroup securityGroup) {
    this.securityGroup = securityGroup;
  }

  public ClassType getClassType() {
    return classType;
  }

  public void setClassType(ClassType classType) {
    this.classType = classType;
  }
}
