package controllers;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.sql.Date;
import java.sql.Time;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import org.doctester.testbrowser.Request;
import org.doctester.testbrowser.Response;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.amazonaws.services.ec2.model.Address;
import com.amazonaws.services.ec2.model.InstanceState;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.appdynamics.aws.AwsAdaptor;
import com.appdynamics.aws.AwsAdaptor.Region;

import dao.SimpleDao;
import models.ClassTypeDetail;
import models.Instance;
import models.ScheduledClass;

@RunWith(MockitoJUnitRunner.class)
public class ScheduledClassesControllerDocTest extends AuthenticatedDocTesterBase {
  static String CLASS_URL = "/api/admin/classes";
  private static DateFormat formatter= new SimpleDateFormat("yyyy-MM-dd");

  SimpleDao<ClassTypeDetail> ctdDao;
  SimpleDao<Instance> iDao;
  ScheduledClassesController controller;

  @Mock
  AwsAdaptor aws;

  @Before
  @Override
  public void init() {
    makeRequest(Request.GET().url(testServerUrl().path("/db/reset?fixture=ClassData")));
    super.init();

    ctdDao = getInjector().getInstance(SimpleDao.class);
    iDao = getInjector().getInstance(SimpleDao.class);
    controller = getInjector().getInstance(ScheduledClassesController.class);
    controller.aws = aws;
  }

  @Test
  public void getAllClasses() {
    sayNextSection("Retrieve all available stored Scheduled Classes.");

    say("Retrieving all available Scheduled Classes is a GET request to " + CLASS_URL);

    Response response = sayAndMakeRequest(
      Request.GET()
        .url(testServerUrl().path(CLASS_URL))
        .addHeader("X-AUTH-TOKEN", auth.auth_token)
      );

    List<ScheduledClass> types = response.payloadAs(List.class);

    sayAndAssertThat("All available Types are returned.",
        types.size(), CoreMatchers.is(2));
  }

  @Test
  public void getScheduledClass() {
    sayNextSection("Retrieve one Scheduled Class.");

    say("Retrieving one Scheduled Class is a GET request to " + CLASS_URL + "/[id]");

    Response response = sayAndMakeRequest(
      Request.GET()
        .url(testServerUrl().path(CLASS_URL + "/1"))
        .addHeader("X-AUTH-TOKEN", auth.auth_token)
      );

    ScheduledClass clazz = response.payloadAs(ScheduledClass.class);

    sayAndAssertThat("Requested Scheduled Class is returned.",
        clazz.getDescription(), CoreMatchers.is("test"));
  }

  @Test
  public void createClass() {
    sayNextSection("Insert Scheduled Class.");

    say("Inserting a Scheduled Class is a POST request to " + CLASS_URL);

    ScheduledClass clazz = getClassObject();

    Response response = sayAndMakeRequest(
      Request.POST()
        .url(testServerUrl().path(CLASS_URL))
        .contentTypeApplicationJson()
        .payload(clazz)
        .addHeader("X-AUTH-TOKEN", auth.auth_token)
      );

    ScheduledClass rType = response.payloadAs(ScheduledClass.class);

    sayAndAssertThat("Class is inserted.",
        rType.getId(), CoreMatchers.notNullValue());

    Mockito.verify(aws).runInstances(any(Region.class), any(RunInstancesRequest.class), anyString());
  }

  private ScheduledClass getClassObject() {
    ScheduledClass clazz = new ScheduledClass();
    clazz.setDescription("insert_test");
    clazz.setClassTypeDetail(ctdDao.getAll(ClassTypeDetail.class).get(0));
    Calendar cal = new GregorianCalendar();
    cal.add(Calendar.MONTH, 1);
    clazz.setStartDate(formatter.format(new Date(cal.getTimeInMillis())));
    cal.add(Calendar.DAY_OF_MONTH, 2);
    clazz.setEndDate(formatter.format(new Date(cal.getTimeInMillis())));
    clazz.setStartTime(Time.valueOf("09:00:00"));
    clazz.setEndTime(Time.valueOf("16:00:00"));
    clazz.setTimeZone(TimeZone.getDefault().getID());
    return clazz;
  }
  @Test
  public void updateClass() {
    sayNextSection("Update Scheduled Class.");

    say("Updating a Scheduled Class is a PUT request to " + CLASS_URL + "/[id]");


    ScheduledClass clazz = getClassObject();

    Response response = sayAndMakeRequest(
      Request.PUT()
        .url(testServerUrl().path(CLASS_URL + "/1"))
        .contentTypeApplicationJson()
        .payload(clazz)
        .addHeader("X-AUTH-TOKEN", auth.auth_token)
      );

    ScheduledClass rClazz = response.payloadAs(ScheduledClass.class);

    sayAndAssertThat("Class is updated.",
        rClazz.getDescription(), CoreMatchers.is("insert_test"));
  }

  @Test
  public void deleteClass() {
    Instance inst = iDao.getAll(Instance.class).get(0);
    inst.setTerminated(true);
    iDao.update(inst);

    sayNextSection("Delete Scheduled Class.");

    say("Deleting a Scheduled Class is a DELETE request to " + CLASS_URL + "/[id]");


    Response response = sayAndMakeRequest(
      Request.DELETE()
        .url(testServerUrl().path(CLASS_URL + "/1"))
        .addHeader("X-AUTH-TOKEN", auth.auth_token)
      );

    ScheduledClass rClazz = response.payloadAs(ScheduledClass.class);

    sayAndAssertThat("Scheduled Class deleted returned.",
        rClazz.getId(), CoreMatchers.is(1L));
    verify(aws, times(1)).terminateInstances(any(Region.class), any(String[].class));
  }

  @Test
  public void runImage() {
    List<com.amazonaws.services.ec2.model.Instance> mock = new ArrayList<>();
    com.amazonaws.services.ec2.model.Instance in = new com.amazonaws.services.ec2.model.Instance();
    in.setImageId("ID-1");
    in.setInstanceId("i-test");
    mock.add(in);
    when(aws.runInstances(any(Region.class), any(RunInstancesRequest.class), anyString())).thenReturn(mock);

    List<Address> addresses = new ArrayList<>();
    Address addr = new Address();
    addr.setPublicIp("127.0.0.1");
    addresses.add(addr);
    when(aws.getEips(anyString(), any(List.class))).thenReturn(addresses );

    sayNextSection("Running class instances.");

    say("Running instances for a class is a POST request to " + CLASS_URL + "/<class_id>/instances");

    say("Pass additional information in request query: <pre>?count=1</pre>. Set count=0 to run configured number of instances.");

    Response response = sayAndMakeRequest(
      Request.POST()
        .url(testServerUrl().path(CLASS_URL + "/1/instances?count=1"))
        .addHeader("X-AUTH-TOKEN", auth.auth_token)
      );

    List<Instance> result = response.payloadAs(List.class);

    sayAndAssertThat("List of instances is returned.",
        result.size(), CoreMatchers.is(1));
  }
  @Test
  public void getAwsInfo() {
    ArrayList<com.amazonaws.services.ec2.model.Instance> instances = getInstanceList();
    when(aws.getInstances(any(String[].class), any(String.class))).thenReturn(instances);
    String testId = instances.get(0).getInstanceId();

    sayNextSection("Retrieve AWS information.");

    say("Retrieving AWS information for class instances is a GET request to " + CLASS_URL + "/<class_id>/aws");

    Response response = sayAndMakeRequest(
      Request.GET()
        .url(testServerUrl().path(CLASS_URL + "/1/aws"))
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

    sayNextSection("Start class instances.");

    say("Starting all instances for a class is a POST request to " + CLASS_URL + "/<class_id>/aws/start");

    Response response = sayAndMakeRequest(
      Request.POST()
        .url(testServerUrl().path(CLASS_URL + "/1/aws/start"))
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

    sayNextSection("Stop all class instances.");

    say("Stopping all instances for a class is a POST request to " + CLASS_URL + "/<class_id>/aws/stop");

    Response response = sayAndMakeRequest(
      Request.POST()
        .url(testServerUrl().path(CLASS_URL
            + "/1/aws/stop"))
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

    say("Terminating one AWS instance is a POST request to " + CLASS_URL + "/<instance_id>/terminate");

    Response response = sayAndMakeRequest(
      Request.POST()
        .url(testServerUrl().path(CLASS_URL
            + "/1/aws/terminate"))
        .addHeader("X-AUTH-TOKEN", auth.auth_token)
      );

    String instancesResp = response.payloadAsString();

    sayAndAssertThat("Terminated instance ID is returned.",
        instancesResp, CoreMatchers.containsString(testId));

  }

  private ArrayList<com.amazonaws.services.ec2.model.Instance> getInstanceList() {
    com.amazonaws.services.ec2.model.Instance testInstance = new com.amazonaws.services.ec2.model.Instance()
        .withInstanceId("ami-xxxxxx")
        .withState(new InstanceState().withName("stopped").withCode(24));
    ArrayList<com.amazonaws.services.ec2.model.Instance> instances = new ArrayList<>();

    instances.add(testInstance);
    return instances;
  }
}
