/**
 * Copyright (C) 2016 AppDynamics
 *
 * @author William Dimaculangan
 */
package controllers;

import static org.junit.Assert.assertThat;
import org.doctester.testbrowser.Request;
import org.doctester.testbrowser.Response;
import static org.hamcrest.CoreMatchers.*;
import org.junit.Before;
import org.junit.Test;

import ninja.NinjaDocTester;

public class AuthenticationControllerTest extends NinjaDocTester {
  static String AUTH_URL = "/api/authenticate";

  @Before
  public void resetDb() {
    makeRequest(Request.GET().url(testServerUrl().path("/db/reset")));
  }

  @Test
  public void testSuccessfulAuth() {
    sayNextSection("Authenication request.");

    say("User authentication is a POST request to " + AUTH_URL);
    Response response = sayAndMakeRequest(
      Request.POST().url(testServerUrl().path(AUTH_URL + "?username=admin&password=admin"))
      );

    AuthenticationController.AuthenicationResponse auth =
        response.payloadAs(AuthenticationController.AuthenicationResponse.class);

    sayAndAssertThat("We get back a token",
        auth.auth_token, notNullValue());
  }

  @Test
  public void testInvalidAuth() {
    Response response = makeRequest(
      Request.POST().url(testServerUrl().path(AUTH_URL + "?username=admin&password=foo"))
      );


    assertThat("We get back an unauthorized response",
        response.httpStatus, is(401));
  }
}
