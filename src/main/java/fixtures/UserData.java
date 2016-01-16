package fixtures;

import java.util.HashMap;

import javax.persistence.EntityManager;

import models.User;

public class UserData implements Fixture {

  @Override
  public void run(EntityManager entityManager, HashMap<String, String> results) {
    User user = new User();
    user.setFirstName("First");
    user.setLastName("Last");
    user.setEmail("firstlast@test.com");
    entityManager.persist(user);

    results.put("UserData", "Inserted one record");
  }

}
