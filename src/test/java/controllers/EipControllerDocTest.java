/**
 * Copyright (C) 2016 AppDynamics
 *
 * @author William Dimaculangan
 */
package controllers;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
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
import org.mockito.runners.MockitoJUnitRunner;

import com.appdynamics.aws.AwsAdaptor;
import com.appdynamics.aws.AwsAdaptor.Region;
import com.google.inject.Inject;

import models.Eip;

@RunWith(MockitoJUnitRunner.class)
public class EipControllerDocTest extends AuthenticatedDocTesterBase{

  private static final String TEST_IP = "127.0.0.1";
  private static final String EIP_URL = "/api/admin/eips";
  private static final String AWS_EIP_URL  = "/api/admin/aws/[region]/eips";

  @Inject
  EipController controller;
  @Mock
  AwsAdaptor aws;

  @Before
  @Override
  public void init() {
    makeRequest(Request.GET().url(testServerUrl().path("/db/reset?fixture=EipData")));
    super.init();

    controller = getInjector().getInstance(EipController.class);
    controller.aws = aws;
  }

  @Test
  public void getAllEip() {
    sayNextSection("Retrieve all available stored EIPs.");

    say("Retrieving all available EIPs is a GET request to " + EIP_URL);

    Response response = sayAndMakeRequest(
      Request.GET()
        .url(testServerUrl().path(EIP_URL))
        .addHeader("X-AUTH-TOKEN", auth.auth_token)
      );

    List<Eip> Eip = response.payloadAs(List.class);

    sayAndAssertThat("All available Eip are returned.",
        Eip.size(), CoreMatchers.is(2));

  }

  @Test
  public void createEip() {
    sayNextSection("Saving an Eip in the application.");

    say("Saving an Eip in the appilcation is a POST request to " + EIP_URL);

    Eip Eip = new Eip();
    Eip.setRegion("REGION");
    Eip.setDescription("DESCRIPTION");
    Eip.setPublicIp(TEST_IP);
    Response response = sayAndMakeRequest(
      Request.POST()
        .url(testServerUrl().path(EIP_URL))
        .contentTypeApplicationJson()
        .payload(Eip)
        .addHeader("X-AUTH-TOKEN", auth.auth_token)
      );

    Eip resultEip = response.payloadAs(Eip.class);

    sayAndAssertThat("Inserted Eip is returned.",
        resultEip.getPublicIp(), CoreMatchers.is(TEST_IP));

  }

  @Test
  public void deleteEip() {
    sayNextSection("Removing a EIP reference from the application.");

    say("Removing an EIP in the appilcation is a DELETE request to " + EIP_URL + "/<eip_id>");
    say("This releases the EIP in AWS also.");

    Response response = sayAndMakeRequest(
    Request.DELETE()
      .url(testServerUrl().path(EIP_URL + "/1"))
      .addHeader("X-AUTH-TOKEN", auth.auth_token)
    );

    Eip resultEip = response.payloadAs(Eip.class);

    sayAndAssertThat("Deleted Eip is returned.",
        resultEip.getPublicIp(), CoreMatchers.is(TEST_IP));

  }


  @Test
  public void getAwsEip() {
    ArrayList<com.amazonaws.services.ec2.model.Address> Eip
        = getEipList();
    when(aws.getEips((anyString()))).thenReturn(Eip);

    sayNextSection("Retrieve all available AWS Eip in a region.");

    say("Retrieving all available AWS Eip in a region is a GET request to " + AWS_EIP_URL);

    say("This retrieves only private Eip.");

    Response response = sayAndMakeRequest(
      Request.GET()
        .url(testServerUrl().path(AWS_EIP_URL.replaceAll("\\[region\\]", Region.AP_SOUTHEAST_2.toString())))
        .addHeader("X-AUTH-TOKEN", auth.auth_token)
      );

    List<Eip> EipResp = response.payloadAs(List.class);

    sayAndAssertThat("All available Eip are returned.",
        EipResp.size(), CoreMatchers.is(1));

  }

  @Test
  public void allocateAwsEip() {
    ArrayList<com.amazonaws.services.ec2.model.Address> eip
        = getEipList();
    String ip = eip.get(0).getPublicIp();
    when(aws.requestEip(anyString(), anyBoolean())).thenReturn(ip);
    when(aws.getEips(anyString(), any(List.class))).thenReturn(eip);

    sayNextSection("Allocate a new AWS Eip in a region.");

    say("Allocating a new AWS Eip in a region is a POST request to " + AWS_EIP_URL);

    say("Pass the paramater 'vpc' to allocate an EIP for usage in vpc");

    Response response = sayAndMakeRequest(
      Request.POST()
        .url(testServerUrl().path(AWS_EIP_URL.replaceAll("\\[region\\]", Region.AP_SOUTHEAST_2.toString())))
        .addHeader("X-AUTH-TOKEN", auth.auth_token)
      );

    Eip EipResp = response.payloadAs(Eip.class);

    sayAndAssertThat("All available Eip are returned.",
        EipResp.getPublicIp(), CoreMatchers.is(ip));

  }


  private ArrayList<com.amazonaws.services.ec2.model.Address> getEipList() {
    com.amazonaws.services.ec2.model.Address testEip = new com.amazonaws.services.ec2.model.Address();
    testEip.setPublicIp(TEST_IP);
    ArrayList<com.amazonaws.services.ec2.model.Address> Eip
        = new ArrayList<>();

    Eip.add(testEip);
    return Eip;
  }
}
