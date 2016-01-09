package controllers;

import java.util.Collections;
import java.util.List;

import javax.inject.Singleton;

import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

import dao.SimpleDao;
import filters.TokenFilter;
import models.ClassType;
import models.ClassTypeDetail;
import ninja.FilterWith;
import ninja.Result;
import ninja.Results;
import ninja.jaxy.DELETE;
import ninja.jaxy.GET;
import ninja.jaxy.POST;
import ninja.jaxy.PUT;
import ninja.jaxy.Path;
import ninja.params.PathParam;

@Path("/api/admin")
@FilterWith(TokenFilter.class)
@Singleton
public class ClassTypeController {
  @Inject
  SimpleDao<ClassType> ctDao;
  @Inject
  SimpleDao<ClassTypeDetail> detailDao;

  @Path("/class_types")
  @GET
  @Transactional
  public Result getAllTypes() {
    ctDao.clearSession();
    List<ClassType> types = ctDao.getAll(ClassType.class);

    Result result = Results.json().render(types);
    return result;
  }

  @Path("/class_types/{id}")
  @GET
  @Transactional
  public Result getType(@PathParam("id") String id) {
    ClassType type = ctDao.find(id, ClassType.class);
    return Results.json().render(type);
  }

  @Path("/class_types")
  @POST
  @Transactional
  public Result insertType(ClassType type) {
    ctDao.persist(type);
    return Results.json().render(type);
  }

  @Path("/class_types/{id}")
  @PUT
  @Transactional
  public Result updateType(@PathParam("id") String id, ClassType type) {
    type.setId(Long.valueOf(id));
    ctDao.update(type);
    return Results.json().render(type);
  }

  @Path("/class_types/{id}")
  @DELETE
  @Transactional
  public Result deleteType(@PathParam("id") String id) {
    System.out.println("In delete****");
    ClassType type = ctDao.delete(Long.valueOf(id), ClassType.class);
    type.setDetails(Collections.EMPTY_LIST);
    return Results.json().render(type);
  }

  /*
   * Details handling
   */

  @Path("/class_types/{typeId}/details")
  @POST
  @Transactional
  public Result insertDetail(@PathParam("typeId") String typeId, ClassTypeDetail detail) {
    ClassType classType = ctDao.find(typeId, ClassType.class);
    detail.setClassType(classType);
    detailDao.persist(detail);
    return Results.json().render(detail);
  }

  @Path("/class_types/{typeId}/details/{id}")
  @PUT
  @Transactional
  public Result updateDetail(@PathParam("typeId") Long typeId,
      @PathParam("id") String id, ClassTypeDetail detail) {
    ClassType classType = ctDao.find(typeId, ClassType.class);
    detail.setClassType(classType);
    detailDao.update(detail);
    return Results.json().render(detail);
  }

  @Path("/class_types/{typeId}/details/{id}")
  @DELETE
  @Transactional
  public Result deleteDetail(@PathParam("typeId") Long typeId,
      @PathParam("id") Long id) {
    ClassTypeDetail detail = detailDao.delete(id, ClassTypeDetail.class);
    return Results.json().render(detail);
  }
}
