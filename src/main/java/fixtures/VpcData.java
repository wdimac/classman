package fixtures;

import java.util.HashMap;

import javax.persistence.EntityManager;

import models.Vpc;

public class VpcData implements Fixture {

  @Override
  public void run(EntityManager em, HashMap<String, String> results) {
    Vpc vpc = new Vpc();
    vpc.setVpcId("vpc-1");
    vpc.setSubnetId("subnet-1");
    em.persist(vpc);

    results.put("Vpc fixture", "1 records inserted");
  }

}
