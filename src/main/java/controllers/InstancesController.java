package controllers;

import java.util.Collections;
import java.util.List;

import javax.inject.Singleton;

import com.appdynamics.aws.AwsAdaptor;
import com.appdynamics.aws.AwsAdaptor.Region;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

import dao.SimpleDao;
import filters.TokenFilter;
import models.Eip;
import models.Instance;
import ninja.FilterWith;
import ninja.Result;
import ninja.Results;
import ninja.jaxy.GET;
import ninja.jaxy.POST;
import ninja.jaxy.Path;
import ninja.params.PathParam;

@Path("/api/admin")
@FilterWith(TokenFilter.class)
@Singleton
public class InstancesController {
  public enum Actions{START, STOP, TERMINATE};

  @Inject
  SimpleDao<Instance> instanceDao;
  @Inject
  SimpleDao<Eip> eipDao;
  @Inject
  AwsAdaptor aws;

  /**
   * Get list of all instances
   * @return
   */
  @Path("/instances")
  @GET
  @Transactional
  public Result getInstances() {
    return Results.json().render(instanceDao.getAll(Instance.class));
  }

  /**
   * Insert new instance information.
   *
   * @param instance
   * @return
   */
  @Path("/instances")
  @POST
  @Transactional
  public Result addInstance(Instance instance) {

    instanceDao.persist(instance);

    return Results.json().render(instance);
  }

  /**
   * Control instance.
   * @param id
   * @return
   */
  @Path("/aws/{region}/instances/{id}/{action}")
  @POST
  @Transactional
  public Result controlInstance(@PathParam("id") String id, @PathParam("action") String actionType) {
    Actions action = Actions.valueOf(actionType.toUpperCase());
    Instance instance = instanceDao.find(id, Instance.class);
    Region region = Region.valueOf(instance.getRegion());
    String[] idList = {id};
    List<String> result=null;
    switch (action) {
    case START:
      result = aws.startInstances(region, idList);
      break;
    case STOP:
      result = aws.stopInstances(region, idList);
      break;
    case TERMINATE:
      result = aws.terminateInstances(region, idList);
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
      break;

    default:
      return Results.badRequest().json();
    }

    return Results.json().render(result);
  }


  /*
   * AWS calls
   */

  @Path("/aws/{region}/instances/{id}")
  @GET
  public Result getAwsInstances(@PathParam("region") String region, @PathParam("id") String id) {
    String[] ids = {id};
    List<com.amazonaws.services.ec2.model.Instance> Instances = aws.getInstances(ids, region);

    return Results.json().render(Instances.get(0));
  }

  @Path("/aws/{region}/instances")
  @GET
  public Result getAllAwsInstances(@PathParam("region") String region) {
    if (region == null) {
      return Results.json().render(Collections.EMPTY_LIST);
    }
    List<com.amazonaws.services.ec2.model.Instance> Instances = aws.getInstances(Region.valueOf(region));

    return Results.json().render(Instances);
  }
}
