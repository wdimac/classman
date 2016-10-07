package models;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name="user")
public class User {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  Long id;

  @Column(name="first_name")
  private String firstName;
  @Column(name="last_name")
  private String lastName;
  private String email;

  @OneToMany(mappedBy="poolUser", fetch=FetchType.EAGER)
  private List<Eip> eips;

  @OneToMany(mappedBy="instructor", fetch=FetchType.EAGER)
  @JsonIgnoreProperties({"instructor"})
  private List<SecurityGroup> securityGroups;
  
  public Long getId() {
    return id;
  }
  public void setId(Long id) {
    this.id = id;
  }
  public String getFirstName() {
    return firstName;
  }
  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }
  public String getLastName() {
    return lastName;
  }
  public void setLastName(String lastName) {
    this.lastName = lastName;
  }
  public String getEmail() {
    return email;
  }
  public void setEmail(String email) {
    this.email = email;
  }
  public List<Eip> getEips() {
    return eips;
  }
  public void setEips(List<Eip> eips) {
    this.eips = eips;
  }
  public List<SecurityGroup> getSecurityGroups() {
    return securityGroups;
  }
  public void setSecurityGroups(List<SecurityGroup> securityGroups) {
    this.securityGroups = securityGroups;
  }
}
