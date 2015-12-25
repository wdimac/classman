package models;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Test {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  Long id;

  public Long getId() {
    return id;
  }

  private String title;

  public Test() {
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }
}
