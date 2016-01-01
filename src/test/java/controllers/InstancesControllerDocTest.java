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
import org.mockito.runners.MockitoJUnitRunner;

import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceState;
import com.appdynamics.aws.AwsAdaptor;
import com.appdynamics.aws.AwsAdaptor.Region;
import com.google.inject.Inject;

@RunWith(MockitoJUnitRunner.class)
public class InstancesControllerDocTest extends AuthenticatedDocTesterBase{

  private static final String INSTANCES_URL = "/api/admin/instances";
  private static final String AWS_INSTANCES_URL  = "/api/admin/aws/[region]/instances";

  @Inject
  InstancesController controller;
  @Mock
  AwsAdaptor aws;

  @Before
  @Override
  public void init() {
    makeRequest(Request.GET().url(testServerUrl().path("/db/reset?fixture=InstanceData")));
    super.init();

    controller = getInjector().getInstance(InstancesController.class);
    controller.aws = aws;
  }

  @Test
  public void getAllInstances() {
    sayNextSection("Retrieve all available stored instances.");

    say("Retrieving all available instances is a GET request to " + INSTANCES_URL);

    Response response = sayAndMakeRequest(
      Request.GET()
        .url(testServerUrl().path(INSTANCES_URL))
        .addHeader("X-AUTH-TOKEN", auth.auth_token)
      );

    List<Instance> instances = response.payloadAs(List.class);

    sayAndAssertThat("All available instances are returned.",
        instances.size(), CoreMatchers.is(2));

  }

  @Test
  public void createInstance() {
    sayNextSection("Saving an instance in the application.");

    say("Saving an instance reference in the appilcation is a POST request to " + INSTANCES_URL);

    models.Instance instance = new models.Instance();
    instance.setId("TEST_ID");
    instance.setRegion("US_EAST_1");
    instance.setDescription("DESCRIPTION");
    instance.setImage_id("ID-1");
    Response response = sayAndMakeRequest(
      Request.POST()
        .url(testServerUrl().path(INSTANCES_URL))
        .contentTypeApplicationJson()
        .payload(instance)
        .addHeader("X-AUTH-TOKEN", auth.auth_token)
      );

    models.Instance resultInstance = response.payloadAs(models.Instance.class);

    sayAndAssertThat("Inserted instance is returned.",
        resultInstance.getId(), CoreMatchers.is(instance.getId()));

  }

  @Test
  public void getOneAwsInstance() {
    ArrayList<Instance> instances = getInstanceList();
    when(aws.getInstances(any(String[].class), any(String.class))).thenReturn(instances);
    String testId = instances.get(0).getInstanceId();

    sayNextSection("Retrieve one AWS instance.");

    say("Retrieving one AWS instance is a GET request to " + AWS_INSTANCES_URL + "/<instance_id>");

    Response response = sayAndMakeRequest(
      Request.GET()
        .url(testServerUrl().path(AWS_INSTANCES_URL.replaceAll("\\[region\\]", Region.AP_SOUTHEAST_2.toString()) + "/" + testId))
        .addHeader("X-AUTH-TOKEN", auth.auth_token)
      );

    String instancesResp = response.payloadAsString();

    sayAndAssertThat("Requested instance is returned.",
        instancesResp, CoreMatchers.containsString(testId));

  }

  @Test
  public void startInstance() {
    String testId = "i-INST1";
    List<String> instances = new ArrayList<>();
    instances.add(testId);
    when(aws.startInstances(any(Region.class), any(String[].class))).thenReturn(instances);

    sayNextSection("Start one AWS instance.");

    say("Starting one AWS instance is a POST request to " + AWS_INSTANCES_URL + "/<instance_id>/start");

    Response response = sayAndMakeRequest(
      Request.POST()
        .url(testServerUrl().path(AWS_INSTANCES_URL
            .replaceAll("\\[region\\]", Region.AP_SOUTHEAST_2.toString())
            + "/" + testId + "/start"))
        .addHeader("X-AUTH-TOKEN", auth.auth_token)
      );

    String instancesResp = response.payloadAsString();

    sayAndAssertThat("Started instance is returned.",
        instancesResp, CoreMatchers.containsString(testId));

  }

  @Test
  public void stopInstance() {
    String testId = "i-INST1";
    List<String> instances = new ArrayList<>();
    instances.add(testId);
    when(aws.stopInstances(any(Region.class), any(String[].class))).thenReturn(instances);

    sayNextSection("Stop one AWS instance.");

    say("Stopping one AWS instance is a POST request to " + AWS_INSTANCES_URL + "/<instance_id>/stop");

    Response response = sayAndMakeRequest(
      Request.POST()
        .url(testServerUrl().path(AWS_INSTANCES_URL
            .replaceAll("\\[region\\]", Region.AP_SOUTHEAST_2.toString())
            + "/" + testId + "/stop"))
        .addHeader("X-AUTH-TOKEN", auth.auth_token)
      );

    String instancesResp = response.payloadAsString();

    sayAndAssertThat("Stopped instance ID is returned.",
        instancesResp, CoreMatchers.containsString(testId));

  }

  @Test
  public void terminateInstance() {
    String testId = "i-INST1";
    List<String> instances = new ArrayList<>();
    instances.add(testId);
    when(aws.terminateInstances(any(Region.class), any(String[].class))).thenReturn(instances);

    sayNextSection("Terminate one AWS instance.");

    say("Terminating one AWS instance is a POST request to " + AWS_INSTANCES_URL + "/<instance_id>/terminate");

    Response response = sayAndMakeRequest(
      Request.POST()
        .url(testServerUrl().path(AWS_INSTANCES_URL
            .replaceAll("\\[region\\]", Region.AP_SOUTHEAST_2.toString())
            + "/" + testId + "/terminate"))
        .addHeader("X-AUTH-TOKEN", auth.auth_token)
      );

    String instancesResp = response.payloadAsString();

    sayAndAssertThat("Terminated instance ID is returned.",
        instancesResp, CoreMatchers.containsString(testId));

  }
private ArrayList<Instance> getInstanceList() {
    Instance testInstance = new Instance()
        .withInstanceId("ami-xxxxxx")
        .withState(new InstanceState().withName("stopped"));
    ArrayList<Instance> instances = new ArrayList<>();

    instances.add(testInstance);
    return instances;
  }
}
