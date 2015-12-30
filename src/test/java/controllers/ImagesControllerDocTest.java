/**
 * Copyright (C) 2016 AppDynamics
 *
 * @author William Dimaculangan
 */
package controllers;

import static org.mockito.Mockito.*;
import java.util.ArrayList;
import java.util.List;

import org.doctester.testbrowser.Request;
import org.doctester.testbrowser.Response;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.amazonaws.services.ec2.model.Image;
import com.appdynamics.aws.AwsAdaptor;
import com.appdynamics.aws.AwsAdaptor.Region;
import com.google.inject.Inject;

import models.Images;

@RunWith(MockitoJUnitRunner.class)
public class ImagesControllerDocTest extends AuthenticatedDocTesterBase{

  private static final String IMAGES_URL = "/api/admin/images";
  private static final String AWS_IMAGES_URL  = "/api/admin/aws/images";

  @Inject
  ImagesController controller;
  @Mock
  AwsAdaptor aws;

  @Before
  @Override
  public void init() {
    makeRequest(Request.GET().url(testServerUrl().path("/db/reset?fixture=ImageData")));
    super.init();

    controller = getInjector().getInstance(ImagesController.class);
    controller.aws = aws;
  }

  @Test
  public void getAllImages() {
    sayNextSection("Retrieve all available stored images.");

    say("Retrieving all available images is a GET request to " + IMAGES_URL);

    Response response = sayAndMakeRequest(
      Request.GET()
        .url(testServerUrl().path(IMAGES_URL))
        .addHeader("X-AUTH-TOKEN", auth.auth_token)
      );

    List<Images> images = response.payloadAs(List.class);

    sayAndAssertThat("All available images are returned.",
        images.size(), CoreMatchers.is(2));

  }

  @Test
  public void createImage() {
    sayNextSection("Saving an image in the application.");

    say("Saving an image in the appilcation is a POST request to " + IMAGES_URL);

    Images image = new Images();
    image.setId("TEST_ID");
    image.setRegion("REGION");
    image.setDescription("DESCRIPTION");
    Response response = sayAndMakeRequest(
      Request.POST()
        .url(testServerUrl().path(IMAGES_URL))
        .contentTypeApplicationJson()
        .payload(image)
        .addHeader("X-AUTH-TOKEN", auth.auth_token)
      );

    Images resultImage = response.payloadAs(Images.class);

    sayAndAssertThat("Inserted image is returned.",
        resultImage.getId(), CoreMatchers.is(image.getId()));

  }

  @Test
  public void deleteImage() {
    sayNextSection("Removing an image reference from the application.");

    say("Removing an image in the appilcation is a DELETE request to " + IMAGES_URL + "/<image_id>");

    Images image = new Images();
    image.setId("TEST_ID");
    image.setRegion("REGION");
    image.setDescription("DESCRIPTION");
    Response response = sayAndMakeRequest(
      Request.DELETE()
        .url(testServerUrl().path(IMAGES_URL + "/ID-1"))
        .contentTypeApplicationJson()
        .payload(image)
        .addHeader("X-AUTH-TOKEN", auth.auth_token)
      );

    Images resultImage = response.payloadAs(Images.class);

    sayAndAssertThat("Deleted image is returned.",
        resultImage.getId(), CoreMatchers.is("ID-1"));

  }

  @Test
  public void getAwsImages() {
    ArrayList<Image> images = getImageList();
    when(aws.getImages(Mockito.any(Region.class))).thenReturn(images);

    sayNextSection("Retrieve all available AWS images in a region.");

    say("Retrieving all available AWS images in a region is a GET request to " + AWS_IMAGES_URL + "/<region>");

    say("This retrieves only private images.");

    Response response = sayAndMakeRequest(
      Request.GET()
        .url(testServerUrl().path(AWS_IMAGES_URL + "/" + Region.AP_SOUTHEAST_2))
        .addHeader("X-AUTH-TOKEN", auth.auth_token)
      );

    List<Images> imagesResp = response.payloadAs(List.class);

    sayAndAssertThat("All available images are returned.",
        imagesResp.size(), CoreMatchers.is(1));

  }

  @Test
  public void getOneAwsImage() {
    ArrayList<Image> images = getImageList();
    when(aws.getImages(any(String[].class), any(String.class))).thenReturn(images);
    String testId = images.get(0).getImageId();

    sayNextSection("Retrieve one AWS images.");

    say("Retrieving one AWS image is a GET request to " + AWS_IMAGES_URL + "/<region>/<image_id>");

    Response response = sayAndMakeRequest(
      Request.GET()
        .url(testServerUrl().path(AWS_IMAGES_URL + "/" + Region.AP_SOUTHEAST_2 + "/" + testId))
        .addHeader("X-AUTH-TOKEN", auth.auth_token)
      );

    String imagesResp = response.payloadAsString();

    sayAndAssertThat("Requested image is returned.",
        imagesResp, CoreMatchers.containsString(testId));

  }

  private ArrayList<Image> getImageList() {
    Image testImage = new Image();
    testImage.setImageId("ami-xxxxxx");
    testImage.setState("available");
    ArrayList<Image> images = new ArrayList<>();

    images.add(testImage);
    return images;
  }
}
