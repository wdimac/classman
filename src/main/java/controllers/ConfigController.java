package controllers;

import java.util.HashMap;

import javax.inject.Singleton;

import com.appdynamics.aws.AwsAdaptor;

import filters.TokenFilter;
import ninja.FilterWith;
import ninja.Result;
import ninja.Results;
import ninja.jaxy.Path;

@Path("/api/admin")
@FilterWith(TokenFilter.class)
@Singleton
public class ConfigController {


  @Path("/config")
  public Result getConfig() {
    HashMap<String, Object> config = new HashMap<>();

    config.put("regions", AwsAdaptor.Region.getNameList());
    return Results.json().render(config);
  }
}
