package models;

import java.sql.Date;
import java.sql.Time;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;

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

  @Column(name="student_count")
  private int studentCount;
  @Column(name="start_date")
  private Date startDate;
  @Column(name="end_date")
  private Date endDate;

  @Column(name="start_time")
  private Time startTime;
  @Column(name="end_time")
  private Time endTime;

  @Column(name="time_zone")
  private String timeZone;

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
  public Date getStartDate() {
    return startDate;
  }
  public void setStartDate(Date startDate) {
    this.startDate = startDate;
  }
  public Date getEndDate() {
    return endDate;
  }
  public void setEndDate(Date endDate) {
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
  public int getStudentCount() {
    return studentCount;
  }
  public void setStudentCount(int studentCount) {
    this.studentCount = studentCount;
  }
}
