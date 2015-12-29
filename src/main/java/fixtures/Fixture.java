package fixtures;

import java.util.HashMap;

import javax.persistence.EntityManager;

public interface Fixture {
  public void run(EntityManager entityManager, HashMap<String, String> results);
}
