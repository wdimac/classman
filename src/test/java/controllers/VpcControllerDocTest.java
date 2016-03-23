/**
 * Copyright (C) 2016 AppDynamics
 *
 * @author William Dimaculangan
 */
package controllers;

import static org.mockito.Mockito.when;

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

import com.appdynamics.aws.AwsAdaptor;
import com.appdynamics.aws.AwsAdaptor.Region;
import com.google.inject.Inject;

import models.Vpc;

@RunWith(MockitoJUnitRunner.class)
public class VpcControllerDocTest extends AuthenticatedDocTesterBase{

  private static final String VPC_GROUP_URL = "/api/admin/vpc";
  private static final String AWS_VPC_GROUP_URL  = "/api/admin/aws/[region]/vpc";

  @Inject
  VpcController controller;
  @Mock
  AwsAdaptor aws;

  @Before
  @Override
  public void init() {
    makeRequest(Request.GET().url(testServerUrl().path("/db/reset?fixture=VpcData")));
    super.init();

    controller = getInjector().getInstance(VpcController.class);
    controller.aws = aws;
  }

  @Test
  public void getAllVpc() {
    sayNextSection("Retrieve all available stored VPC.");

    say("Retrieving all available VPC is a GET request to " + VPC_GROUP_URL);

    Response response = sayAndMakeRequest(
      Request.GET()
        .url(testServerUrl().path(VPC_GROUP_URL))
        .addHeader("X-AUTH-TOKEN", auth.auth_token)
      );

    List<Vpc> vpc = response.payloadAs(List.class);

    sayAndAssertThat("All available VPC are returned.",
        vpc.size(), CoreMatchers.is(1));

  }

  @Test
  public void createVpc() {
    sayNextSection("Saving an VPC in the application.");

    say("Saving an VPC in the appilcation is a POST request to " + VPC_GROUP_URL);

    Vpc vpc = new Vpc();
    vpc.setVpcId("TEST_ID");
    vpc.setSubnetId("subnet-test");
    vpc.setRegion("REGION");
    Response response = sayAndMakeRequest(
      Request.POST()
        .url(testServerUrl().path(VPC_GROUP_URL))
        .contentTypeApplicationJson()
        .payload(vpc)
        .addHeader("X-AUTH-TOKEN", auth.auth_token)
      );

    Vpc resultVpc = response.payloadAs(Vpc.class);

    sayAndAssertThat("Inserted securityGroup is returned.",
        resultVpc.getSubnetId(), CoreMatchers.is(vpc.getSubnetId()));

  }

  @Test
  public void deleteVpc() {
    sayNextSection("Removing a VPC reference from the application.");

    say("Removing an VPC in the appilcation is a DELETE request to " + VPC_GROUP_URL + "/<group_id>");

      Response response = sayAndMakeRequest(
      Request.DELETE()
        .url(testServerUrl().path(VPC_GROUP_URL + "/subnet-1"))
        .addHeader("X-AUTH-TOKEN", auth.auth_token)
      );

    Vpc resultVpc = response.payloadAs(Vpc.class);

    sayAndAssertThat("Deleted securityGroup is returned.",
        resultVpc.getSubnetId(), CoreMatchers.is("subnet-1"));

  }


  @Test
  public void getAwsVPc() {
    List<com.amazonaws.services.ec2.model.Subnet> subnets
        = getSubnetList();
    when(aws.getAllVpcs(Mockito.any(Region.class))).thenReturn(subnets);

    sayNextSection("Retrieve all available AWS VPC in a region.");

    say("Retrieving all available AWS VPC in a region is a GET request to " + AWS_VPC_GROUP_URL);

    Response response = sayAndMakeRequest(
      Request.GET()
        .url(testServerUrl().path(AWS_VPC_GROUP_URL.replaceAll("\\[region\\]", Region.AP_SOUTHEAST_2.toString())))
        .addHeader("X-AUTH-TOKEN", auth.auth_token)
      );

    List<Vpc> vpcResp = response.payloadAs(List.class);

    sayAndAssertThat("All available securityGroup are returned.",
        vpcResp.size(), CoreMatchers.is(1));

  }


  private ArrayList<com.amazonaws.services.ec2.model.Subnet> getSubnetList() {
    com.amazonaws.services.ec2.model.Subnet testSubnet =
        new com.amazonaws.services.ec2.model.Subnet()
        .withVpcId("vpc-xxxxxx")
        .withSubnetId("subnet-xxx");
    ArrayList<com.amazonaws.services.ec2.model.Subnet> sList
        = new ArrayList<>();

    sList.add(testSubnet);
    return sList;
  }
}
