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
import ninja.FilterWith;
import ninja.Result;
import ninja.Results;
import ninja.jaxy.DELETE;
import ninja.jaxy.GET;
import ninja.jaxy.POST;
import ninja.jaxy.Path;
import ninja.jpa.UnitOfWork;
import ninja.params.PathParam;

@Path("/api/admin")
@FilterWith(TokenFilter.class)
@Singleton
public class VpcController {
  @Inject
  SimpleDao<models.Vpc> vpcDao;
  @Inject
  AwsAdaptor aws;

  /**
   * Get list of VPC
   * @return
   */
  @Path("/vpc")
  @GET
  @Transactional
  public Result getVpc() {
    return Results.json().render(vpcDao.getAll(models.Vpc.class));
  }

  /**
   * Insert new VPC information.
   *
   * @param vpc
   * @return
   */
  @Path("/vpc")
  @POST
  @Transactional
  public Result addVpc(models.Vpc vpc) {

    vpcDao.persist(vpc);

    return Results.json().render(vpc);
  }

  @Path("/vpc/{id}")
  @DELETE
  @Transactional
  public Result deleteVpc(@PathParam("id") String id) {
    models.Vpc vpc = vpcDao.delete(id, models.Vpc.class);

    return Results.json().render(vpc);
  }


  /*
   * AWS calls
   */

  @Path("/aws/{region}/vpc")
  @GET
  @UnitOfWork
  public Result getAllAwsSubnet(@PathParam("region") String region) {
    if (region == null) {
      return Results.json().render(Collections.EMPTY_LIST);
    }
    List<com.amazonaws.services.ec2.model.Subnet> subnet = aws.getAllVpcs(Region.valueOf(region));

    return Results.json().render(subnet);
  }

}
