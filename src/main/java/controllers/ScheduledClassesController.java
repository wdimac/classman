package controllers;

import java.sql.Date;
import java.sql.Time;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.ec2.model.Address;
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
import models.SecurityGroup;
import ninja.FilterWith;
import ninja.Result;
import ninja.Results;
import ninja.jaxy.DELETE;
import ninja.jaxy.GET;
import ninja.jaxy.POST;
import ninja.jaxy.PUT;
import ninja.jaxy.Path;
import ninja.jpa.UnitOfWork;
import ninja.params.Param;
import ninja.params.PathParam;
import scheduled.ClassManager;

@Path("/api/admin")
@FilterWith(TokenFilter.class)
@Singleton
public class ScheduledClassesController {
  static final Logger log = LoggerFactory.getLogger(ScheduledClassesController.class);
  private static final int DAYS = 1000 * 60 *60 * 24;
  private static final long HOURS8 = 1000 * 60 *60 * 8;
  private static final long MIN_15 = 1000 * 60 * 15;
  private static DateFormat formatter= new SimpleDateFormat("yyyy-MM-dd");

  @Inject
  SimpleDao<ScheduledClass> scDao;
  @Inject
  SimpleDao<ClassTypeDetail> ctdDao;
  @Inject
  SimpleDao<models.Instance> instanceDao;
  @Inject
  SimpleDao<Eip> eipDao;
  @Inject
  SimpleDao<SecurityGroup> groupDao;

  @Inject
  AwsAdaptor aws;
  @Inject
  ClassManager cm;

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
    ClassTypeDetail detail = ctdDao.find(clazz.getClassTypeDetail().getId(), ClassTypeDetail.class);
    if (clazz.getEndDate() == null) {
      Date sDate = Date.valueOf(clazz.getStartDate());
      clazz.setEndDate(formatter.format(new Date(
          sDate.getTime() +
          (detail.getClassType().getDuration() - 1) * DAYS)));
    }
    if (clazz.getEndTime() == null) {
      clazz.setEndTime(new Time(clazz.getStartTime().getTime() + HOURS8));
    }
    
    SecurityGroup group = groupDao.find(clazz.getSecurityGroup().getId(), SecurityGroup.class);
    log.info("GROUP:" + clazz.getSecurityGroup().getId());
    scDao.persist(clazz);
    scDao.refresh(clazz); // Load details

    try {
      startClassInstances(clazz.getCount(), clazz);
    } catch (Exception e) {
      log.error("Failed to start class instances", e);
    }

    scDao.refresh(clazz); // Load instance information
    cm.setShutdown(clazz.getId(), System.currentTimeMillis() + MIN_15);

    return Results.json().render(clazz);
  }

  @Path("/classes/{id}")
  @PUT
  @Transactional
  public Result updateClass(@PathParam("id") String id, ScheduledClass clazz) {
    scDao.clearSession();
    clazz.setId(Long.valueOf(id));
    scDao.update(clazz);
    return Results.json().render(clazz);
  }

  @Path("/classes/{id}")
  @DELETE
  @Transactional
  public Result deleteClass(@PathParam("id") Long id) {
    ScheduledClass sched = scDao.find(id, ScheduledClass.class);
    List<models.Instance> instancesToTerminate = new ArrayList<>();
    for (models.Instance instance : sched.getInstances()) {
      if (!instance.isTerminated()) {
        instancesToTerminate.add(instance);
      }
    }
    
    if (!instancesToTerminate.isEmpty()) {
      aws.terminateInstances(
          Region.valueOf(sched.getClassTypeDetail().getRegion()), 
          instancesToTerminate.stream().map(i -> i.getId()).toArray(size -> new String[size]));
    }
    
    scDao.delete(Long.valueOf(id), ScheduledClass.class);
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
    List<models.Instance> result = startClassInstances(count, clazz);
    return Results.json().render(result);
  }

  public List<models.Instance> startClassInstances(Integer count, ScheduledClass clazz) {
    if (count == 0) count = clazz.getCount();

    List<Eip> usable = getUsableEips(clazz, count);

    RunInstancesRequest request = new RunInstancesRequest();
    request.setImageId(clazz.getClassTypeDetail().getImageId());
    request.setInstanceType(InstanceType.valueOf(clazz.getClassTypeDetail().getInstanceType()));
    request.setMaxCount(count);
    request.setMinCount(count);
    request.setSecurityGroupIds(new QuickList<String>(clazz.getSecurityGroup().getId()));
    if (clazz.getClassTypeDetail().getSubnet() != null) {
      request.setSubnetId(clazz.getClassTypeDetail().getSubnet().getSubnetId());
    }
    List<Instance> response = aws.runInstances(
        Region.valueOf(clazz.getClassTypeDetail().getRegion().toUpperCase()),
        request,
        clazz.getClassTypeDetail().getClassType().getName());

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


    int idx = 0;
    for (models.Instance inst: result) {
      Eip eip = usable.get(idx);
      eip.setInstance(inst);
      eipDao.persist(eip);
      idx++;
    }

    eipDao.detach(usable);
    aws.associateEipWhenReady(usable);
    return result;
  }

  @Path("/classes/{id}/eips")
  @POST
  @Transactional
  public Result handleEips(@PathParam("id") Long id) {
    ScheduledClass clazz = scDao.find(id, ScheduledClass.class);
    List<models.Instance> instances = clazz.getInstances();
    if(instances.size() > 0 && instances.get(0).getEip() != null) { //remove eips
      dropEips(clazz);
    } else { //add eips
      List<Eip> usable = getUsableEips(clazz, instances.size());
      
      int idx = 0;
      for (models.Instance inst: instances) {
        Eip eip = usable.get(idx);
        eip.setInstance(inst);
        eipDao.persist(eip);
        instanceDao.refresh(inst);
        idx++;
      }

      eipDao.detach(usable);
      aws.associateEipWhenReady(usable);
    }
    return Results.json().render(instances);
  }

  /**
   * Dissociate eips from instances. Release any temporary ones.
   * 
   * @param clazz
   * @param instances
   */
  private void dropEips(ScheduledClass clazz) {
    for(models.Instance instance:clazz.getInstances()) {
      Eip eip = instance.getEip();
      if (eip != null) {
        aws.disassociateEip(instance.getEip());
        if(eip.getPoolUser() == null) {
          aws.releaseEips(clazz.getClassTypeDetail().getRegion(),
            eip.getAllocationId(), eip.getPublicIp());
          eipDao.delete(eip.getId(), Eip.class);
        } else {
          eip.setInstance(null);
          eipDao.update(eip);
        }
      }
    }
  }
  
  /**
   * Get count eips from the users pool. Eip must be currently unassigned and usable
   * in the same domain as the security group being used. (i.e. VPC or standard).
   * If insufficient EIPs are located, allocate new temporary EIPs.
   * @param clazz
   * @param count
   * @return
   */
  private List<Eip> getUsableEips(ScheduledClass clazz, int count) {
    String domain = (clazz.getClassTypeDetail().getSubnet() != null) ? "vpc" : "standard";
    List<Eip> usable = new ArrayList<>();
    if (clazz.getInstructor() != null) {
      for (Eip eip: clazz.getInstructor().getEips()) {
        if ((eip.getInstance() == null)
            && eip.getRegion().equals(clazz.getClassTypeDetail().getRegion())
            && domain.equals(eip.getDomain())) {
          usable.add(eip);
        }
      }
    }

    while (usable.size() < count) {
      String region = clazz.getClassTypeDetail().getRegion();
      String publicIp = aws.requestEip(region, "vpc".equals(domain));
      Address address = aws.getEips(region, new QuickList<>(publicIp)).get(0);
      Eip eip = new Eip();
      eip.loadFromAddress(address);
      eip.setRegion(region);
      // Leave user blank because it is not a pool item.
      eipDao.persist(eip);

      usable.add(eip);
    }
    return usable;
  }

  /**
   * Control all instances.
   * @param id
   * @return
   */
  @Path("/classes/{id}/aws/{action}")
  @POST
  @Transactional
  public Result controlInstances(@PathParam("id") String id, @PathParam("action") String actionType) {
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
  @Transactional
  public Result getAwsInfo(@PathParam("region") String region, @PathParam("id") String id) {
    ScheduledClass cls = scDao.find(id, ScheduledClass.class);
    Map<String, models.Instance> idList = new HashMap<>();
    for (models.Instance inst : cls.getInstances()) {
      if (!inst.isTerminated()) {
        idList.put(inst.getId(), inst);
      }
    }

    if (idList.isEmpty()) {
      return Results.json().render(Collections.EMPTY_LIST);
    }

    List<com.amazonaws.services.ec2.model.Instance> instanceInfos
      = aws.getInstances(new ArrayList<String>(idList.keySet()),
                         cls.getClassTypeDetail().getRegion());

    for (com.amazonaws.services.ec2.model.Instance info: instanceInfos) {
      if (info.getState().getCode() == 48) {
        models.Instance inst = idList.get(info.getInstanceId());
        inst.setTerminated(true);
        instanceDao.update(inst);
        log.info("Updated " + inst.getId() + " " + inst.isTerminated());
      }
    }
    return Results.json().render(instanceInfos);
  }
}
