/**
 * Copyright (C) 2016 AppDynamics
 *
 * @author William Dimaculangan
 */

package controllers;

import java.util.HashMap;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;

import ninja.Result;
import ninja.Results;

@Singleton
public class DbController {
  @Inject
  Provider<EntityManager> entityManagerProvider;

  /**
   * Reset database back to initial state.
   *
   * USE WITH CAUTION. Unavailable in prod.
   *
   * TODO: allow loading of fixtures
   *
   * @return details of records deleted.
   */
  @Transactional
  public Result reset() {
    HashMap<String, String> results = new HashMap<String, String>();
    EntityManager entityManager = entityManagerProvider.get();

    // Clear Test table. Save default initial value.
    Query query = entityManager.createQuery("DELETE from Test where id > 1");
    int testDelete = query.executeUpdate();
    results.put("Items deleted from Test table", String.valueOf(testDelete));

    // Clear Token table.
    query = entityManager.createQuery("DELETE from Token");
    testDelete = query.executeUpdate();
    results.put("Items deleted from Tokens table", String.valueOf(testDelete));

    return Results.json().render(results);
  }
}
