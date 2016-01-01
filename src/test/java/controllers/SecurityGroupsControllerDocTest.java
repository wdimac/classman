/**
 * Copyright (C) 2016 AppDynamics
 *
 * @author William Dimaculangan
 */
package controllers;

import static org.mockito.Matchers.any;
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

import models.SecurityGroup;

@RunWith(MockitoJUnitRunner.class)
public class SecurityGroupsControllerDocTest extends AuthenticatedDocTesterBase{

  private static final String SECURITY_GROUP_URL = "/api/admin/security_groups";
  private static final String AWS_SECURITY_GROUP_URL  = "/api/admin/aws/[region]/security_groups";

  @Inject
  SecurityGroupsController controller;
  @Mock
  AwsAdaptor aws;

  @Before
  @Override
  public void init() {
    makeRequest(Request.GET().url(testServerUrl().path("/db/reset?fixture=SecurityGroupData")));
    super.init();

    controller = getInjector().getInstance(SecurityGroupsController.class);
    controller.aws = aws;
  }

  @Test
  public void getAllsecurityGroup() {
    sayNextSection("Retrieve all available stored security groups.");

    say("Retrieving all available security groups is a GET request to " + SECURITY_GROUP_URL);

    Response response = sayAndMakeRequest(
      Request.GET()
        .url(testServerUrl().path(SECURITY_GROUP_URL))
        .addHeader("X-AUTH-TOKEN", auth.auth_token)
      );

    List<SecurityGroup> securityGroup = response.payloadAs(List.class);

    sayAndAssertThat("All available securityGroup are returned.",
        securityGroup.size(), CoreMatchers.is(2));

  }

  @Test
  public void createsecurityGroup() {
    sayNextSection("Saving an securityGroup in the application.");

    say("Saving an securityGroup in the appilcation is a POST request to " + SECURITY_GROUP_URL);

    SecurityGroup securityGroup = new SecurityGroup();
    securityGroup.setId("TEST_ID");
    securityGroup.setRegion("REGION");
    securityGroup.setDescription("DESCRIPTION");
    securityGroup.setOwnerId("OWN-1");
    Response response = sayAndMakeRequest(
      Request.POST()
        .url(testServerUrl().path(SECURITY_GROUP_URL))
        .contentTypeApplicationJson()
        .payload(securityGroup)
        .addHeader("X-AUTH-TOKEN", auth.auth_token)
      );

    SecurityGroup resultsecurityGroup = response.payloadAs(SecurityGroup.class);

    sayAndAssertThat("Inserted securityGroup is returned.",
        resultsecurityGroup.getId(), CoreMatchers.is(securityGroup.getId()));

  }

  @Test
  public void deletesecurityGroup() {
    sayNextSection("Removing a security group reference from the application.");

    say("Removing an security group in the appilcation is a DELETE request to " + SECURITY_GROUP_URL + "/<group_id>");

      Response response = sayAndMakeRequest(
      Request.DELETE()
        .url(testServerUrl().path(SECURITY_GROUP_URL + "/SG-1"))
        .addHeader("X-AUTH-TOKEN", auth.auth_token)
      );

    SecurityGroup resultsecurityGroup = response.payloadAs(SecurityGroup.class);

    sayAndAssertThat("Deleted securityGroup is returned.",
        resultsecurityGroup.getId(), CoreMatchers.is("SG-1"));

  }


  @Test
  public void getAwssecurityGroup() {
    ArrayList<com.amazonaws.services.ec2.model.SecurityGroup> securityGroup
        = getSecurityGroupList();
    when(aws.getSecurityGroups(Mockito.any(Region.class))).thenReturn(securityGroup);

    sayNextSection("Retrieve all available AWS securityGroup in a region.");

    say("Retrieving all available AWS securityGroup in a region is a GET request to " + AWS_SECURITY_GROUP_URL);

    say("This retrieves only private securityGroup.");

    Response response = sayAndMakeRequest(
      Request.GET()
        .url(testServerUrl().path(AWS_SECURITY_GROUP_URL.replaceAll("\\[region\\]", Region.AP_SOUTHEAST_2.toString())))
        .addHeader("X-AUTH-TOKEN", auth.auth_token)
      );

    List<SecurityGroup> securityGroupResp = response.payloadAs(List.class);

    sayAndAssertThat("All available securityGroup are returned.",
        securityGroupResp.size(), CoreMatchers.is(1));

  }

  @Test
  public void getOneAwssecurityGroup() {
    ArrayList<com.amazonaws.services.ec2.model.SecurityGroup> securityGroup = getSecurityGroupList();
    when(aws.getSecurityGroups(any(String[].class), any(String.class))).thenReturn(securityGroup);
    String testId = securityGroup.get(0).getGroupId();

    sayNextSection("Retrieve one AWS securityGroup.");

    say("Retrieving one AWS securityGroup is a GET request to " + AWS_SECURITY_GROUP_URL + "/<securityGroup_id>");

    Response response = sayAndMakeRequest(
      Request.GET()
        .url(testServerUrl().path(AWS_SECURITY_GROUP_URL.replaceAll("\\[region\\]", Region.AP_SOUTHEAST_2.toString()) + "/" + testId))
        .addHeader("X-AUTH-TOKEN", auth.auth_token)
      );

    String securityGroupResp = response.payloadAsString();

    sayAndAssertThat("Requested securityGroup is returned.",
        securityGroupResp, CoreMatchers.containsString(testId));

  }

  private ArrayList<com.amazonaws.services.ec2.model.SecurityGroup> getSecurityGroupList() {
    com.amazonaws.services.ec2.model.SecurityGroup testsecurityGroup = new com.amazonaws.services.ec2.model.SecurityGroup();
    testsecurityGroup.setGroupId("ami-xxxxxx");
    ArrayList<com.amazonaws.services.ec2.model.SecurityGroup> securityGroup
        = new ArrayList<>();

    securityGroup.add(testsecurityGroup);
    return securityGroup;
  }
}
