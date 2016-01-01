package controllers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Singleton;

import com.amazonaws.services.ec2.model.Image;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceType;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.appdynamics.aws.AwsAdaptor;
import com.appdynamics.aws.AwsAdaptor.Region;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

import dao.ImagesDao;
import dao.InstanceDao;
import filters.TokenFilter;
import models.Images;
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
public class ImagesController {
  @Inject
  ImagesDao imagesDao;
  @Inject
  InstanceDao instanceDao;
  @Inject
  AwsAdaptor aws;

  public static class RunConfiguration {
    String type;
    public String getType() {
      return type;
    }
    public void setType(String type) {
      this.type = type;
    }
    public Integer getCount() {
      return count;
    }
    public void setCount(Integer count) {
      this.count = count;
    }
    Integer count;
  }

  /**
   * Get list of images
   * @return
   */
  @Path("/images")
  @GET
  @Transactional
  public Result getImages() {
    return Results.json().render(imagesDao.getAllImages());
  }

  /**
   * Insert new image information.
   *
   * @param image
   * @return
   */
  @Path("/images")
  @POST
  @Transactional
  public Result addImage(Images image) {

    imagesDao.persist(image);

    return Results.json().render(image);
  }

  @Path("/images/{id}")
  @DELETE
  @Transactional
  public Result deleteImage(@PathParam("id") String id) {
    Images image = imagesDao.delete(id);

    return Results.json().render(image);
  }

  /**
   * Attempt to run the image with the configuration provided.
   *
   * @param id
   * @param runConfig
   * @return List of instances started.
   */
  @Path("/images/{id}/run")
  @POST
  @Transactional
  public Result runImage(@PathParam("id") String id, RunConfiguration runConfig) {
    Images image = imagesDao.find(id);
    RunInstancesRequest request = aws.getRunRequest();
    request.setImageId(image.getId());
    request.setInstanceType(InstanceType.valueOf(runConfig.type));
    request.setMaxCount(runConfig.count);
    request.setMinCount(runConfig.count);
    List<Instance> response = aws.runInstances(Region.valueOf(image.getRegion()), request);

    List<models.Instance> result = new ArrayList<>();
    for (Instance in: response) {
      models.Instance instance = new models.Instance();
      instance.setId(in.getInstanceId());
      instance.setImage_id(in.getImageId());
      instance.setRegion(image.getRegion());
      instance.setDescription("Instance for " + in.getImageId());
      instanceDao.persist(instance);
      result.add(instance);
    }
    return Results.json().render(result);
  }

  /*
   * AWS calls
   */

  @Path("/aws/{region}/images/{id}")
  @GET
  public Result getAwsImages(@PathParam("region") String region, @PathParam("id") String id) {
    String[] ids = {id};
    List<Image> images = aws.getImages(ids, region);

    return Results.json().render(images.get(0));
  }

  @Path("/aws/{region}/images")
  @GET
  public Result getAllAwsImages(@PathParam("region") String region) {
    if (region == null) {
      return Results.json().render(Collections.EMPTY_LIST);
    }
    List<Image> images = aws.getImages(Region.valueOf(region));

    return Results.json().render(images);
  }
}
