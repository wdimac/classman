package fixtures;

import java.util.HashMap;

import javax.persistence.EntityManager;

import models.SecurityGroup;

public class SecurityGroupData implements Fixture {

  @Override
  public void run(EntityManager em, HashMap<String, String> results) {
    SecurityGroup group = new SecurityGroup();
    group.setId("SG-1");
    group.setRegion("US_EAST_1");
    group.setOwnerId("FRED");
    em.persist(group);

    group = new SecurityGroup();
    group.setId("SG-2");
    group.setRegion("US_EAST_1");
    group.setOwnerId("FRED");
    em.persist(group);
    results.put("SecurityGroup fixture", "2 records inserted");
  }

}
