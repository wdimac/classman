package models;

import java.sql.Time;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name="class")
public class ScheduledClass {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  Long id;

  @Column(name="descr")
  private String description;

  @ManyToOne
  @JoinColumn(name="class_type_detail_id")
  private ClassTypeDetail classTypeDetail;

  @ManyToOne
  @JoinColumn(name="instructor_id")
  private User instructor;

  @OneToMany(cascade=CascadeType.REMOVE, mappedBy="scheduledClass", fetch=FetchType.EAGER)
  private List<Instance> instances;

  @Column(name="count")
  private int count;
  @Column(name="start_date_str")
  private String startDate;
  @Column(name="end_date_str")
  private String endDate;

  @Column(name="start_time")
  private Time startTime;
  @Column(name="end_time")
  private Time endTime;

  @Column(name="time_zone")
  private String timeZone;

  @ManyToOne
  @JoinColumn(name="security_group_id")
  private SecurityGroup securityGroup;

  public SecurityGroup getSecurityGroup() {
    return securityGroup;
  }
  public void setSecurityGroup(SecurityGroup securityGroup) {
    this.securityGroup = securityGroup;
  }
  public Long getId() {
    return id;
  }
  public void setId(Long id) {
    this.id = id;
  }
  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }
  public ClassTypeDetail getClassTypeDetail() {
    return classTypeDetail;
  }
  public void setClassTypeDetail(ClassTypeDetail classTypeDetail) {
    this.classTypeDetail = classTypeDetail;
  }
  public User getInstructor() {
    return instructor;
  }
  public void setInstructor(User instructor) {
    this.instructor = instructor;
  }
  public String getStartDate() {
    return startDate;
  }
  public void setStartDate(String startDate) {
    this.startDate = startDate;
  }
  public String getEndDate() {
    return endDate;
  }
  public void setEndDate(String endDate) {
    this.endDate = endDate;
  }
  public Time getStartTime() {
    return startTime;
  }
  public void setStartTime(Time startTime) {
    this.startTime = startTime;
  }
  public Time getEndTime() {
    return endTime;
  }
  public void setEndTime(Time endTime) {
    this.endTime = endTime;
  }
  public String getTimeZone() {
    return timeZone;
  }
  public void setTimeZone(String timeZone) {
    this.timeZone = timeZone;
  }
  public int getCount() {
    return count;
  }
  public void setCount(int count) {
    this.count = count;
  }
  public List<Instance> getInstances() {
    return instances;
  }
  public void setInstances(List<Instance> instances) {
    this.instances = instances;
  }
}
