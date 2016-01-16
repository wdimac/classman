package controllers;

import java.sql.Date;
import java.sql.Time;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import org.doctester.testbrowser.Request;
import org.doctester.testbrowser.Response;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;

import com.google.inject.Inject;

import dao.SimpleDao;
import models.ClassTypeDetail;
import models.ScheduledClass;

public class ScheduledClassesControllerDocTest extends AuthenticatedDocTesterBase {
  static String CLASS_URL = "/api/admin/classes";

  SimpleDao<ClassTypeDetail> ctdDao;

  @Before
  @Override
  public void init() {
    makeRequest(Request.GET().url(testServerUrl().path("/db/reset?fixture=ClassData")));
    super.init();

    ctdDao = getInjector().getInstance(SimpleDao.class);
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
        types.size(), CoreMatchers.is(1));
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
  }

  private ScheduledClass getClassObject() {
    ScheduledClass clazz = new ScheduledClass();
    clazz.setDescription("insert_test");
    clazz.setClassTypeDetail(ctdDao.getAll(ClassTypeDetail.class).get(0));
    Calendar cal = new GregorianCalendar();
    cal.add(Calendar.MONTH, 1);
    clazz.setStartDate(new Date(cal.getTimeInMillis()));
    cal.add(Calendar.DAY_OF_MONTH, 2);
    clazz.setEndDate(new Date(cal.getTimeInMillis()));
    clazz.setStartTime(Time.valueOf("09:00:00"));
    clazz.setEndTime(Time.valueOf("16:00:00"));
    clazz.setTimeZone(TimeZone.getDefault().getID());
    return clazz;
  }
  @Test
  public void updateType() {
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
  public void deleteType() {
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
  }
}
