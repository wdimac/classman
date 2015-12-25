/**
 * Copyright (C) 2016 AppDynamics
 *
 * @author William Dimaculangan
 */

package controllers;

import com.google.inject.Singleton;

import ninja.Result;
import ninja.Results;
import ninja.jaxy.GET;
import ninja.jaxy.Path;

@Path("/")
@Singleton
public class ApplicationController {

  @Path("")
  @GET
  public Result index() {

    return Results.html();

  }

}
