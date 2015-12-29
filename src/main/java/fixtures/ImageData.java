package fixtures;

import java.util.HashMap;

import javax.persistence.EntityManager;

import models.Images;

public class ImageData implements Fixture {

  @Override
  public void run(EntityManager em, HashMap<String, String> results) {
    Images image = new Images();
    image.setId("ID-1");
    image.setRegion("REGION");
    em.persist(image);

    image = new Images();
    image.setId("ID-2");
    image.setRegion("REGION");
    em.persist(image);
    results.put("ImageData fixture", "2 records inserted");
  }

}
