package fixtures;

import java.util.HashMap;

import javax.persistence.EntityManager;

import models.Images;
import models.Instance;

public class InstanceData implements Fixture {

  @Override
  public void run(EntityManager em, HashMap<String, String> results) {
    Images image = new Images();
    image.setId("ID-1");
    image.setRegion("US_EAST_1");
    em.persist(image);

    Instance instance = new Instance();
    instance.setImage_id("ID-1");
    instance.setRegion("US_EAST_1");
    instance.setTerminated(false);
    instance.setId("i-INST1");
    em.persist(instance);

    em.detach(instance);
    instance.setId("i-INST2");
    em.persist(instance);
  }

}