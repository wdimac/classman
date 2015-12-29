package controllers;

import java.util.Collections;
import java.util.List;

import javax.inject.Singleton;

import com.amazonaws.services.ec2.model.Image;
import com.appdynamics.aws.AwsAdaptor;
import com.appdynamics.aws.AwsAdaptor.Region;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

import dao.ImagesDao;
import filters.TokenFilter;
import models.Images;
import ninja.FilterWith;
import ninja.Result;
import ninja.Results;
import ninja.jaxy.GET;
import ninja.jaxy.POST;
import ninja.jaxy.Path;
import ninja.params.Param;
import ninja.params.PathParam;

@Path("/api/admin")
@FilterWith(TokenFilter.class)
@Singleton
public class ImagesController {
  @Inject
  ImagesDao imagesDao;
  @Inject
  AwsAdaptor aws;

  @Path("/images")
  @GET
  @Transactional
  public Result getImages() {
    return Results.json().render(imagesDao.getAllImages());
  }

  @Path("/images")
  @POST
  @Transactional
  public Result addImage(Images image) {

    System.out.println(image);
    imagesDao.persist(image);

    return Results.json().render(image);
  }

  @Path("/aws/images/{region}/{id}")
  @GET
  public Result getAwsImages(@PathParam("region") String region, @PathParam("id") String id) {
    String[] ids = {id};
    List<Image> images = aws.getImages(ids, region);

    return Results.json().render(images.get(0));
  }

  @Path("/aws/images/{region}")
  @GET
  public Result getAllAwsImages(@PathParam("region") String region) {
    if (region == null) {
      return Results.json().render(Collections.EMPTY_LIST);
    }
    List<Image> images = aws.getImages(Region.valueOf(region));

    return Results.json().render(images);
  }
}
