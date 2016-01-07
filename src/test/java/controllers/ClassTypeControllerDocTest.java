package controllers;

import java.util.List;

import org.doctester.testbrowser.Request;
import org.doctester.testbrowser.Response;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;

import com.amazonaws.regions.Regions;

import models.ClassType;
import models.ClassTypeDetail;

public class ClassTypeControllerDocTest extends AuthenticatedDocTesterBase {
  static String CT_URL = "/api/admin/class_types";

  @Before
  @Override
  public void init() {
    makeRequest(Request.GET().url(testServerUrl().path("/db/reset?fixture=ClassTypeData")));
    super.init();
  }

  @Test
  public void getAllTypes() {
    sayNextSection("Retrieve all available stored Class Types.");

    say("Retrieving all available Class Types is a GET request to " + CT_URL);

    Response response = sayAndMakeRequest(
      Request.GET()
        .url(testServerUrl().path(CT_URL))
        .addHeader("X-AUTH-TOKEN", auth.auth_token)
      );

    List<ClassType> types = response.payloadAs(List.class);

    sayAndAssertThat("All available Types are returned.",
        types.size(), CoreMatchers.is(1));
  }

  @Test
  public void getType() {
    sayNextSection("Retrieve one Class Type.");

    say("Retrieving one Class Type is a GET request to " + CT_URL + "/[id]");

    Response response = sayAndMakeRequest(
      Request.GET()
        .url(testServerUrl().path(CT_URL + "/1"))
        .addHeader("X-AUTH-TOKEN", auth.auth_token)
      );

    ClassType type = response.payloadAs(ClassType.class);

    sayAndAssertThat("Requested Class Type is returned.",
        type.getName(), CoreMatchers.is("test"));
    sayAndAssertThat("ClassType contains Details",
        type.getDetails().size(), CoreMatchers.is(1));
  }

  @Test
  public void createType() {
    sayNextSection("Insert Class Type.");

    say("Inserting a Class Type is a POST request to " + CT_URL);

    ClassType type = new ClassType();
    type.setDuration(4);
    type.setName("insert_test");

    Response response = sayAndMakeRequest(
      Request.POST()
        .url(testServerUrl().path(CT_URL))
        .contentTypeApplicationJson()
        .payload(type)
        .addHeader("X-AUTH-TOKEN", auth.auth_token)
      );

    ClassType rType = response.payloadAs(ClassType.class);

    sayAndAssertThat("Class is inserted.",
        rType.getId(), CoreMatchers.notNullValue());
  }
  @Test
  public void updateType() {
    sayNextSection("Update Class Type.");

    say("Updating a Class Type is a PUT request to " + CT_URL + "/[id]");


    ClassType type = new ClassType();
    type.setDuration(4);
    type.setName("insert_test");

    Response response = sayAndMakeRequest(
      Request.PUT()
        .url(testServerUrl().path(CT_URL + "/1"))
        .contentTypeApplicationJson()
        .payload(type)
        .addHeader("X-AUTH-TOKEN", auth.auth_token)
      );

    ClassType rType = response.payloadAs(ClassType.class);

    sayAndAssertThat("Class is updated.",
        rType.getName(), CoreMatchers.is("insert_test"));
  }

  @Test
  public void deleteType() {
    sayNextSection("Delete Class Type.");

    say("Deleting a Class Type is a DELETE request to " + CT_URL + "/[id]");


    Response response = sayAndMakeRequest(
      Request.DELETE()
        .url(testServerUrl().path(CT_URL + "/1"))
        .addHeader("X-AUTH-TOKEN", auth.auth_token)
      );

    ClassType rType = response.payloadAs(ClassType.class);

    sayAndAssertThat("Class Type deleted returned.",
        rType.getId(), CoreMatchers.is(1L));
  }

  /*
   * Detail Handling
   */
  @Test
  public void createDetail() {
    sayNextSection("Insert Class Type Detail.");

    say("Inserting a Class Type is a POST request to " + CT_URL + "/[id]/details");

    ClassTypeDetail detail = new ClassTypeDetail();
    detail.setRegion(Regions.EU_WEST_1.getName());

    Response response = sayAndMakeRequest(
      Request.POST()
        .url(testServerUrl().path(CT_URL + "/1/details"))
        .contentTypeApplicationJson()
        .payload(detail)
        .addHeader("X-AUTH-TOKEN", auth.auth_token)
      );

    ClassTypeDetail rType = response.payloadAs(ClassTypeDetail.class);

    sayAndAssertThat("Class is inserted.",
        rType.getId(), CoreMatchers.notNullValue());
  }
  @Test
  public void updateDetail() {
    sayNextSection("Update Class Type Detail.");

    say("Updating a Class Type Detail is a PUT request to " + CT_URL + "/[type_id]/details/[detail_id]");


    ClassTypeDetail type = new ClassTypeDetail();
    type.setRegion(Regions.EU_WEST_1.getName());

    Response response = sayAndMakeRequest(
      Request.PUT()
        .url(testServerUrl().path(CT_URL + "/1/details/1"))
        .contentTypeApplicationJson()
        .payload(type)
        .addHeader("X-AUTH-TOKEN", auth.auth_token)
      );

    ClassTypeDetail rType = response.payloadAs(ClassTypeDetail.class);

    sayAndAssertThat("Class is updated.",
        rType.getRegion(), CoreMatchers.is(Regions.EU_WEST_1.getName()));
  }

  @Test
  public void deleteDetail() {
    sayNextSection("Delete Class Type Detail.");

    say("Deleting a Class Type is a DELETE request to " + CT_URL + "/[type_id]/details/[id]");


    Response response = sayAndMakeRequest(
      Request.DELETE()
        .url(testServerUrl().path(CT_URL + "/1/details/1"))
        .addHeader("X-AUTH-TOKEN", auth.auth_token)
      );

    ClassTypeDetail rType = response.payloadAs(ClassTypeDetail.class);

    sayAndAssertThat("Class Type deleted returned.",
        rType.getId(), CoreMatchers.is(1L));
  }
}
