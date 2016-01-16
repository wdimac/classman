package controllers;

import java.util.HashMap;
import java.util.TimeZone;

import javax.inject.Singleton;

import com.amazonaws.services.ec2.model.InstanceType;
import com.appdynamics.aws.AwsAdaptor;
import com.google.inject.Inject;

import dao.SimpleDao;
import filters.TokenFilter;
import models.SecurityGroup;
import ninja.FilterWith;
import ninja.Result;
import ninja.Results;
import ninja.jaxy.Path;

@Path("/api/admin")
@FilterWith(TokenFilter.class)
@Singleton
public class ConfigController {
  @Inject
  SimpleDao<SecurityGroup> groupDao;

  @Path("/config")
  public Result getConfig() {
    HashMap<String, Object> config = new HashMap<>();

    config.put("regions", AwsAdaptor.Region.getNameList());
    config.put("instanceTypes", InstanceType.values());
    config.put("timezones", TimeZone.getAvailableIDs());
    return Results.json().render(config);
  }
}
