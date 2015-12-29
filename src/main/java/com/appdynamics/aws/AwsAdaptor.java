package com.appdynamics.aws;

import java.util.ArrayList;
import java.util.List;

import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeImagesRequest;
import com.amazonaws.services.ec2.model.DescribeImagesResult;
import com.amazonaws.services.ec2.model.Filter;
import com.amazonaws.services.ec2.model.Image;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.google.inject.Singleton;

@Singleton
public class AwsAdaptor {
  /**
   * List of Regions we could connect to.
   *
   * Hard-coded here so we don't have to connect to a region
   * to find out what regions we can connect to.
   *
   * @author William Dimaculangan
   */
  public enum Region {
    US_EAST_1	      ("US East (N. Virginia)", "ec2.us-east-1.amazonaws.com"),
    US_WEST_2	      ("US West (Oregon)", "ec2.us-west-2.amazonaws.com"),
    US_WEST_1	      ("US West (N. California)", "ec2.us-west-1.amazonaws.com"),
    EU_WEST_1	      ("EU (Ireland)", "ec2.eu-west-1.amazonaws.com"),
    EU_CENTRAL_1	  ("EU (Frankfurt)", "ec2.eu-central-1.amazonaws.com"),
    AP_SOUTHEAST_1	("Asia Pacific (Singapore)", "ec2.ap-southeast-1.amazonaws.com"),
    AP_SOUTHEAST_2	("Asia Pacific (Sydney)", "ec2.ap-southeast-2.amazonaws.com"),
    AP_NORTHEAST_1	("Asia Pacific (Tokyo)", "ec2.ap-northeast-1.amazonaws.com"),
    SA_EAST_1	      ("South America (Sao Paulo)", "ec2.sa-east-1.amazonaws.com");

    private String name;
    private String endpoint;
    public String getName() {
      return name;
    }
    public String getEndpoint() {
      return endpoint;
    }

    Region(String name, String endpoint){
      this.name = name;
      this.endpoint = endpoint;
    }
    Region(){};

    public static ArrayList<String[]> getNameList() {

      ArrayList<String[]> nameList = new ArrayList<>();
      for (Region region: Region.values()){
        String[] foo = {region.toString(), region.getName()};
        nameList.add(foo);
      }
      return nameList;
    }
  }

  private static final String DEFAULT_SECURITY_GROUP = "EDU_HTTP-RDP";

  /**
   * Finds all private Images available.
   *
   * @param region
   * @return
   */
  public List<Image> getImages(Region region) {
    AmazonEC2Client amazonClient = getClient(region);
    DescribeImagesResult result = amazonClient
        .describeImages(
            new DescribeImagesRequest().withFilters(
                new Filter("is-public").withValues("false")
            ));
    return result.getImages();
  }

  /**
   * Return all items listed
   * AmazonEC2Client amazonClient = getClient(region);
   * @param ids
   * @return
   */
  public List<Image> getImages(String[] ids, String region) {
    AmazonEC2Client amazonClient = getClient(Region.valueOf(region));
    DescribeImagesResult result = amazonClient
        .describeImages(
            new DescribeImagesRequest().withImageIds(ids));
    return result.getImages();
  }

  /**
   * Provide a request object with the security group already set.
   *
   * @return
   */
  public RunInstancesRequest getRunRequest() {
    return new RunInstancesRequest()
        .withSecurityGroups(DEFAULT_SECURITY_GROUP);
  }

  /**
   * Attempt to start the instances.
   *
   * @param region
   * @param request
   * @return
   */
  public List<Instance> startInstance(Region region, RunInstancesRequest request) {
    AmazonEC2Client amazonClient = getClient(region);
    RunInstancesResult results = amazonClient.runInstances(request);
    return results.getReservation().getInstances();
  }

  /**
   * Private method to establish a client connection.
   *
   * @param region
   * @return
   */
  private AmazonEC2Client getClient(Region region) {
    AmazonEC2Client amazonEC2Client = new AmazonEC2Client();
    amazonEC2Client.setEndpoint(region.getEndpoint());
    return amazonEC2Client;
  }

}
