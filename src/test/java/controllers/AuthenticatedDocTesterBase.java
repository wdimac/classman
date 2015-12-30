/**
 * Copyright (C) 2016 AppDynamics
 *
 * @author William Dimaculangan
 */
package controllers;

import org.doctester.testbrowser.Request;
import org.doctester.testbrowser.Response;
import org.junit.Before;

import ninja.NinjaDocTester;

/**
 * Parent class for DocTester tests requiring authentication
 *
 * @author William Dimaculangan
 *
 */
public class AuthenticatedDocTesterBase extends NinjaDocTester {
  protected AuthenticationController.AuthenicationResponse auth = null;

  @Before
  public void init() {
    Response response = makeRequest(
        Request.POST().url(testServerUrl().path("/api/authenticate?username=admin&password=admin"))
        );
    auth =response.payloadAs(AuthenticationController.AuthenicationResponse.class);
  }
}
