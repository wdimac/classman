package fixtures;

import java.util.HashMap;

import javax.persistence.EntityManager;

import models.Eip;

public class EipData implements Fixture {

  @Override
  public void run(EntityManager em, HashMap<String, String> results) {
    Eip image = new Eip();
    image.setRegion("US_EAST_1");
    image.setPublicIp("127.0.0.1");
    em.persist(image);

    image = new Eip();
    image.setPublicIp("127.0.0.1");
    em.persist(image);
    results.put("ImageData fixture", "2 records inserted");
  }

}
