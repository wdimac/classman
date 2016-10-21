package models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name="instances")
public class Instance {
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
  private String image_id;
  @Column(name="is_terminated")
  private boolean isTerminated;

  @ManyToOne
  @JoinColumn(name="class_id", updatable=false)
  @JsonIgnoreProperties({"instances"})
  private ScheduledClass scheduledClass;
  
  @OneToOne(mappedBy="instance")
  @JsonIgnoreProperties({"instance"})
  private Eip eip;

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

  public boolean isTerminated() {
    return isTerminated;
  }
  public void setTerminated(boolean isTerminated) {
    this.isTerminated = isTerminated;
  }
  public String getImage_id() {
    return image_id;
  }
  public void setImage_id(String image_id) {
    this.image_id = image_id;
  }
  public ScheduledClass getScheduledClass() {
    return scheduledClass;
  }
  public void setScheduledClass(ScheduledClass scheduledClass) {
    this.scheduledClass = scheduledClass;
  }
  public Eip getEip() {
    return eip;
  }
  public void setEip(Eip eip) {
    this.eip = eip;
  }

}
