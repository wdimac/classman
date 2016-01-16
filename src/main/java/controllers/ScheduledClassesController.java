package controllers;

import java.sql.Date;
import java.sql.Time;
import java.util.List;

import javax.inject.Singleton;

import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

import dao.SimpleDao;
import filters.TokenFilter;
import models.ClassTypeDetail;
import models.ScheduledClass;
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
public class ScheduledClassesController {
  private static final int DAYS = 1000 * 60 *60 * 24;
  private static final long HOURS8 = 1000 * 60 *60 * 8;

  @Inject
  SimpleDao<ScheduledClass> scDao;
  @Inject
  SimpleDao<ClassTypeDetail> ctdDao;

  @Path("/classes")
  @GET
  @Transactional
  public Result getAllClasses() {
    scDao.clearSession();
    List<ScheduledClass> classes = scDao.getAll(ScheduledClass.class);

    Result result = Results.json().render(classes);
    return result;
  }

  @Path("/classes/{id}")
  @GET
  @Transactional
  public Result getScheduledClass(@PathParam("id") String id) {
    ScheduledClass clazz = scDao.find(id, ScheduledClass.class);
    return Results.json().render(clazz);
  }

  @Path("/classes")
  @POST
  @Transactional
  public Result insertClass(ScheduledClass clazz) {
    System.out.println(clazz.getClassTypeDetail().getId());
    ClassTypeDetail detail = ctdDao.find(clazz.getClassTypeDetail().getId(), ClassTypeDetail.class);
    if (clazz.getEndDate() == null) {
      clazz.setEndDate(new Date(
          clazz.getStartDate().getTime() +
          (detail.getClassType().getDuration() - 1) * DAYS));
    }
    if (clazz.getEndTime() == null) {
      clazz.setEndTime(new Time(clazz.getStartTime().getTime() + HOURS8));
    }
    scDao.persist(clazz);
    return Results.json().render(clazz);
  }

  @Path("/classes/{id}")
  @PUT
  @Transactional
  public Result updateClass(@PathParam("id") String id, ScheduledClass clazz) {
    clazz.setId(Long.valueOf(id));
    scDao.update(clazz);
    return Results.json().render(clazz);
  }

  @Path("/classes/{id}")
  @DELETE
  @Transactional
  public Result deleteClass(@PathParam("id") Long id) {
    ScheduledClass sched = scDao.delete(Long.valueOf(id), ScheduledClass.class);
    return Results.json().render(sched);
  }

}
