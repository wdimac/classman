package controllers;

import java.util.Collections;
import java.util.List;

import javax.inject.Singleton;

import com.amazonaws.services.ec2.model.SecurityGroup;
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
import ninja.params.PathParam;

@Path("/api/admin")
@FilterWith(TokenFilter.class)
@Singleton
public class SecurityGroupsController {
  @Inject
  SimpleDao<models.SecurityGroup> securityGroupDao;
  @Inject
  AwsAdaptor aws;

  /**
   * Get list of security groups
   * @return
   */
  @Path("/security_groups")
  @GET
  @Transactional
  public Result getSecurityGroups() {
    return Results.json().render(securityGroupDao.getAll(models.SecurityGroup.class));
  }

  /**
   * Insert new securityGroup information.
   *
   * @param securityGroup
   * @return
   */
  @Path("/security_groups")
  @POST
  @Transactional
  public Result addsecurityGroup(models.SecurityGroup securityGroup) {

    securityGroupDao.persist(securityGroup);

    return Results.json().render(securityGroup);
  }

  @Path("/security_groups/{id}")
  @DELETE
  @Transactional
  public Result deletesecurityGroup(@PathParam("id") String id) {
    models.SecurityGroup securityGroup = securityGroupDao.delete(id, models.SecurityGroup.class);

    return Results.json().render(securityGroup);
  }


  /*
   * AWS calls
   */

  @Path("/aws/{region}/security_groups")
  @GET
  public Result getAllAwssecurityGroups(@PathParam("region") String region) {
    if (region == null) {
      return Results.json().render(Collections.EMPTY_LIST);
    }
    List<SecurityGroup> securityGroups = aws.getSecurityGroups(Region.valueOf(region));

    return Results.json().render(securityGroups);
  }

  @Path("/aws/{region}/security_groups/{id}")
  @GET
  public Result getOneAwssecurityGroup(@PathParam("region") String region, @PathParam("id") String id) {
    if (region == null) {
      return Results.json().render(Collections.EMPTY_LIST);
    }

    List<SecurityGroup> securityGroups = aws.getSecurityGroups(new String[]{id}, region);

    return Results.json().render(securityGroups.get(0));
  }
}
