/**
 * Copyright (C) 2016 AppDynamics
 *
 * @author William Dimaculangan
 */

package controllers;

import java.util.List;

import org.doctester.testbrowser.Request;
import org.doctester.testbrowser.Response;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

public class TestDataControllerDocTesterTest extends AuthenticatedDocTesterBase {
  static String TEST_DATA_URL = "/api/testdata";

  @Test
  public void testGetAll () {
    sayNextSection("Retrieve all Test data.");

    say("Retrieving all Test data is a GET request to " + TEST_DATA_URL);

    Response response = sayAndMakeRequest(
      Request.GET()
        .url(testServerUrl().path(TEST_DATA_URL))
        .addHeader("X-AUTH-TOKEN", auth.auth_token)
      );

    List<models.Test> tests = response.payloadAs(List.class);

    sayAndAssertThat("We get back all test items",
        tests.size(), CoreMatchers.is(1));
  }

  @Test
  public void testGetOne () {
    sayNextSection("Retrieve individual Test data.");

    say("Retrieving all Test data is a GET request to " + TEST_DATA_URL);

    Response response = sayAndMakeRequest(
      Request.GET()
        .url(testServerUrl().path(TEST_DATA_URL + "/1"))
        .addHeader("X-AUTH-TOKEN", auth.auth_token)
      );

    models.Test test = response.payloadAs(models.Test.class);

    sayAndAssertThat("We get back the requested item.",
        test.getTitle(), CoreMatchers.is("First"));
  }

}
