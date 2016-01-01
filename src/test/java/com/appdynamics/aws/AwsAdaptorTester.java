/**
 * Copyright (C) 2016 AppDynamics
 *
 * @author William Dimaculangan
 */

package com.appdynamics.aws;

import java.util.List;

import com.amazonaws.services.ec2.model.Image;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceType;
import com.appdynamics.aws.AwsAdaptor.Region;

/**
 * This class will make a connection to AWS,
 * so should not be run as a unit test.
 *
 * @author William Dimaculangan
 */
public class AwsAdaptorTester {

  public static void main(String[] args) {
    Region testRegion = Region.AP_SOUTHEAST_2;
    AwsAdaptor adaptor = new AwsAdaptor();
    List<Image> images = adaptor.getImages(testRegion);
    System.out.println("Images available:");
    for (Image image: images) {
      System.out.println("Id: " + image.getImageId() + " Name: " + image.getName());
    }
    System.out.print("\nEnter ID of image to start:");
    String imageId = System.console().readLine();

    List<Instance> instances = adaptor.runInstances(testRegion,
        adaptor.getRunRequest()
          .withImageId(imageId)
          .withInstanceType(InstanceType.M3Xlarge)
          .withMaxCount(1)
          .withMinCount(1)
        );

    System.out.println("Instance started:");
    System.out.println(instances);
  }
}
