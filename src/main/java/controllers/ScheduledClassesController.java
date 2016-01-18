package controllers;

import java.sql.Date;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Singleton;

import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceType;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.appdynamics.aws.AwsAdaptor;
import com.appdynamics.aws.AwsAdaptor.Region;
import com.appdynamics.aws.QuickList;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

import controllers.InstancesController.Actions;
import dao.SimpleDao;
import filters.TokenFilter;
import models.ClassTypeDetail;
import models.Eip;
import models.ScheduledClass;
import ninja.FilterWith;
import ninja.Result;
import ninja.Results;
import ninja.jaxy.DELETE;
import ninja.jaxy.GET;
import ninja.jaxy.POST;
import ninja.jaxy.PUT;
import ninja.jaxy.Path;
import ninja.params.Param;
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
  @Inject
  SimpleDao<models.Instance> instanceDao;
  @Inject
  SimpleDao<Eip> eipDao;

  @Inject
  AwsAdaptor aws;

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

  /*
   * Instance Management
   */

  @Path("/classes/{id}/instances")
  @POST
  @Transactional
  public Result startInstances(@PathParam("id") Long id, @Param("count") Integer count) {
    if (count == null || count < 0) {
      return Results.badRequest().json();
    }
    ScheduledClass clazz = scDao.find(id, ScheduledClass.class);
    if (count == 0) count = clazz.getCount();
    RunInstancesRequest request = new RunInstancesRequest();
    request.setImageId(clazz.getClassTypeDetail().getImageId());
    request.setInstanceType(InstanceType.valueOf(clazz.getClassTypeDetail().getInstanceType()));
    request.setMaxCount(count);
    request.setMinCount(count);
    request.setSecurityGroupIds(new QuickList<String>(clazz.getClassTypeDetail().getSecurityGroupId()));
    List<Instance> response = aws.runInstances(Region.valueOf(clazz.getClassTypeDetail().getRegion().toUpperCase()), request);

    List<models.Instance> result = new ArrayList<>();
    for (Instance in: response) {
      models.Instance instance = new models.Instance();
      instance.setId(in.getInstanceId());
      instance.setImage_id(in.getImageId());
      instance.setRegion(clazz.getClassTypeDetail().getRegion());
      instance.setDescription("Instance for " + clazz.getClassTypeDetail().getClassType().getName());
      instance.setScheduledClass(clazz);
      instanceDao.persist(instance);
      result.add(instance);
    }
    return Results.json().render(result);
  }

  /**
   * Control all instances.
   * @param id
   * @return
   */
  @Path("/classes/{id}/aws/{action}")
  @POST
  @Transactional
  public Result controlInstance(@PathParam("id") String id, @PathParam("action") String actionType) {
    Actions action = Actions.valueOf(actionType.toUpperCase());
    scDao.clearSession();
    ScheduledClass cls = scDao.find(id, ScheduledClass.class);
    List<String> idList = new ArrayList<>();
    for (models.Instance inst : cls.getInstances()) {
      if (!inst.isTerminated()) {
        idList.add(inst.getId());
      }
    }

    if (idList.isEmpty()) {
      return Results.json().render(idList);
    }

    Region region = Region.valueOf(cls.getClassTypeDetail().getRegion());
    List<String> result=null;
    switch (action) {
    case START:
      result = aws.startInstances(region, idList.toArray(new String[idList.size()]));
      break;
    case STOP:
      result = aws.stopInstances(region, idList.toArray(new String[idList.size()]));
      break;
    case TERMINATE:
      result = aws.terminateInstances(region, idList.toArray(new String[idList.size()]));
      for (models.Instance instance: cls.getInstances()) {
        if (result.contains(instance.getId())) {
          instance.setTerminated(true);
          Eip qEip = new Eip();
          qEip.setInstanceId(id);
          Eip eip = eipDao.findBy(qEip);
          if (eip != null) {
            aws.disassociateEip(eip.getRegion(), eip.getPublicIp());
            eip.setInstanceId(null);
            eipDao.persist(eip);
          }
          instanceDao.persist(instance);
        }
      }
      break;

    default:
      return Results.badRequest().json();
    }

    return Results.json().render(result);
  }

  @Path("/classes/{id}/aws")
  @GET
  public Result getAwsInfo(@PathParam("region") String region, @PathParam("id") String id) {
    ScheduledClass cls = scDao.find(id, ScheduledClass.class);
    List<String> idList = new ArrayList<>();
    for (models.Instance inst : cls.getInstances()) {
      if (!inst.isTerminated()) {
        idList.add(inst.getId());
      }
    }

    if (idList.isEmpty()) {
      return Results.json().render(idList);
    }

    List<com.amazonaws.services.ec2.model.Instance> instanceInfos
      = aws.getInstances(idList.toArray(new String[idList.size()]),
                         cls.getClassTypeDetail().getRegion());

    return Results.json().render(instanceInfos);
  }
}
