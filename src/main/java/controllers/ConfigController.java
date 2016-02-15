package controllers;

import java.util.Arrays;
import java.util.HashMap;
import java.util.TimeZone;

import javax.inject.Singleton;
import javax.transaction.Transactional;

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
  @Transactional
  public Result getConfig() {
    HashMap<String, Object> config = new HashMap<>();

    config.put("regions", AwsAdaptor.Region.getNameList());
    config.put("instanceTypes", InstanceType.values());
    String[] zones = TimeZone.getAvailableIDs();
    Arrays.sort(zones);
    config.put("timezones", zones);
    return Results.json().render(config);
  }
}
