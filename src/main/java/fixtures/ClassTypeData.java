package fixtures;

import java.util.HashMap;

import javax.persistence.EntityManager;

import com.amazonaws.regions.Regions;

import models.ClassType;
import models.ClassTypeDetail;

public class ClassTypeData implements Fixture {

  @Override
  public void run(EntityManager entityManager, HashMap<String, String> results) {
    ClassType type = new ClassType();
    type.setName("test");
    type.setDuration(4);
    entityManager.persist(type);

    ClassTypeDetail detail = new ClassTypeDetail();
    detail.setRegion(Regions.US_EAST_1.getName());
    detail.setClassType(type);
    entityManager.persist(detail);
  }

}
