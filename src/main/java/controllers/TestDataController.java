/**
 * Copyright (C) 2016 AppDynamics
 *
 * @author William Dimaculangan
 */

package controllers;

import java.util.List;

import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import com.google.inject.Inject;
import com.google.inject.Provider;

import filters.TokenFilter;
import models.Test;
import ninja.FilterWith;
import ninja.Result;
import ninja.Results;
import ninja.jaxy.GET;
import ninja.jaxy.Path;
import ninja.params.PathParam;

@Path("/api")
@FilterWith(TokenFilter.class)
@Singleton
public class TestDataController {
  @Inject
  Provider<EntityManager> entityManagerProvider;

  @Path("/testdata")
  @GET
  public Result getAll() {
    EntityManager entityManager = entityManagerProvider.get();

    TypedQuery<Test> q = entityManager.createQuery("SELECT x FROM Test x", Test.class);
    List<Test> tests = q.getResultList();

    return Results.json().render(tests);
  }

  @Path("/testdata/{id}")
  @GET
  public Result get(@PathParam("id") long id) {
    EntityManager entityManager = entityManagerProvider.get();

    TypedQuery<Test> q = entityManager.createQuery("SELECT x FROM Test x WHERE x.id= :idparam", Test.class);
    Test test = q.setParameter("idparam", id).getSingleResult();
    return Results.json().render(test);
  }

}
