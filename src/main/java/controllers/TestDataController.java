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

import models.Test;
import ninja.Result;
import ninja.Results;
import ninja.jaxy.GET;
import ninja.jaxy.Path;
import ninja.jpa.UnitOfWork;
import ninja.params.PathParam;

@Path("/api")
@Singleton
public class TestDataController {
  @Inject
  Provider<EntityManager> entityManagerProvider;

  @Path("/testdata")
  @GET
  @UnitOfWork
  public Result getAll() {
    EntityManager entityManager = entityManagerProvider.get();

    TypedQuery<Test> q = entityManager.createQuery("SELECT x FROM Test x", Test.class);
    List<Test> tests = q.getResultList();

    return Results.json().render(tests);
  }

  @Path("/testdata/{id}")
  @GET
  @UnitOfWork
  public Result get(@PathParam("id") long id) {
    EntityManager entityManager = entityManagerProvider.get();

    System.out.println(">>>Retrieving:" + id);
    TypedQuery<Test> q = entityManager.createQuery("SELECT x FROM Test x WHERE x.id= :idparam", Test.class);
    Test test = q.setParameter("idparam", id).getSingleResult();
    System.out.println("Found:" + test.getId() + " " + test.getTitle());
    return Results.json().render(test);
  }

}
