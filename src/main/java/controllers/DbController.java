/**
 * Copyright (C) 2016 AppDynamics
 *
 * @author William Dimaculangan
 */

package controllers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;

import fixtures.Fixture;
import ninja.Result;
import ninja.Results;
import ninja.params.Param;

@Singleton
public class DbController {
  @Inject
  Provider<EntityManager> entityManagerProvider;

  /**
   * Reset database back to initial state.
   *
   * USE WITH CAUTION. Unavailable in prod.
   *
   * @return details of records deleted.
   */
  @Transactional
  public Result reset(@Param("fixture") String[] fixtures) {
    String[] tables = {"Token", "Images"};

    HashMap<String, String> results = new HashMap<String, String>();
    EntityManager entityManager = entityManagerProvider.get();

    // Clear Test table. Save default initial value.
    Query query = entityManager.createQuery("DELETE from Test where id > 1");
    int testDelete = query.executeUpdate();
    results.put("Items deleted from Test table", String.valueOf(testDelete));

    // Clear tables.
    for (String table: tables) {
      query = entityManager.createQuery("DELETE from " + table);
      testDelete = query.executeUpdate();
      results.put("Items deleted from " + table + " table", String.valueOf(testDelete));
    }

    // Load Fixtures
    if (fixtures != null) {
      for (String fixture: fixtures) {
        try {
          Class fixtureClass = Class.forName("fixtures." + fixture);
          Fixture fix = (Fixture)fixtureClass.newInstance();
          if (fix instanceof Fixture) {
            fix.run(entityManager, results);
          }
        } catch (Exception e) {
          results.put("Fixture: " + fixture, "Failed to run Fixture " + fixture + " - " + e.getMessage());
          e.printStackTrace();
        }
      }
    }
    return Results.json().render(results);
  }
}
