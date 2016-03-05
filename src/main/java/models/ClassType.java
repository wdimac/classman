package models;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name="class_type")
public class ClassType {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  Long id;

  private String name;
  private int duration;
  @OneToMany( cascade=CascadeType.REMOVE, mappedBy="classType", fetch=FetchType.EAGER)
  private List<ClassTypeDetail> details;

  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }
  public int getDuration() {
    return duration;
  }
  public void setDuration(int duration) {
    this.duration = duration;
  }
  public Long getId() {
    return id;
  }
  public void setId(Long id) {
    this.id = id;
  }
  public List<ClassTypeDetail> getDetails() {
    return details;
  }
  public void setDetails(List<ClassTypeDetail> details) {
    this.details = details;
  }
}
