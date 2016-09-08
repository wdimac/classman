/**
 * Copyright (C) 2016 AppDynamics
 *
 * @author William Dimaculangan
 */
package controllers;

import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import org.doctester.testbrowser.Request;
import org.doctester.testbrowser.Response;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

public class ConfigControllerDocTest extends AuthenticatedDocTesterBase{

  private static final String CONFIG_URL = "/api/admin/config";

  @Test
  public void testConfig() {
    sayNextSection("Retrieve static configuration data.");

    say("Retrieving all configuration data is a GET request to " + CONFIG_URL);

    Response response = sayAndMakeRequest(
      Request.GET()
        .url(testServerUrl().path(CONFIG_URL))
        .addHeader("X-AUTH-TOKEN", auth.auth_token)
      );

    HashMap<String, List<Object>> config = response.payloadAs(HashMap.class);

    sayAndAssertThat("Config contains 9 Regions.",
        ((List<Object>)config.get("regions")).size(), CoreMatchers.is(9));
    sayAndAssertThat("Config contains 59 Instance Types.",
        ((List<Object>)config.get("instanceTypes")).size(), CoreMatchers.is(59));
    sayAndAssertThat("Config conatins all Timezone IDs",
        ((List<Object>)config.get("timezones")).size(), CoreMatchers.is(TimeZone.getAvailableIDs().length));
  }

}
