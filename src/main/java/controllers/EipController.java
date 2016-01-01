package controllers;

import java.util.Collections;
import java.util.List;

import javax.inject.Singleton;

import com.amazonaws.services.ec2.model.Address;
import com.appdynamics.aws.AwsAdaptor;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

import dao.SimpleDao;
import filters.TokenFilter;
import models.Eip;
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
public class EipController {
  @Inject
  SimpleDao<models.Eip> eipDao;
  @Inject
  AwsAdaptor aws;

  /**
   * Get list of elastic ips
   * @return
   */
  @Path("/eips")
  @GET
  @Transactional
  public Result getEips() {
    return Results.json().render(eipDao.getAll(models.Eip.class));
  }

  /**
   * Insert new elastic ip information.
   *
   * @param elasticIp
   * @return
   */
  @Path("/eips")
  @POST
  @Transactional
  public Result addEip(models.Eip eip) {

    eipDao.persist(eip);

    return Results.json().render(eip);
  }

  @Path("/eips/{id}")
  @DELETE
  @Transactional
  public Result deleteEip(@PathParam("id") String id) {
    models.Eip Eip = eipDao.delete(Long.valueOf(id), models.Eip.class);

    return Results.json().render(Eip);
  }

  /**
   * Update Elastic ip information.
   *
   * @param elasticIp
   * @return
   */
  @Path("/eips/{id}")
  @PUT
  @Transactional
  public Result updateEip(@PathParam("id") String id, @Param("instanceId") String instanceId) {
    System.out.println(instanceId);
    Eip eip = eipDao.find(id, Eip.class);
    eip.setInstanceId(instanceId);

    eipDao.persist(eip);

    return Results.json().render(eip);
  }

  /*
   * AWS calls
   */

  @Path("/aws/{region}/eips")
  @GET
  public Result getAllAwsEips(@PathParam("region") String region) {
    if (region == null) {
      return Results.json().render(Collections.EMPTY_LIST);
    }
    List<Address> Eips = aws.getEips(region);

    return Results.json().render(Eips);
  }

}
