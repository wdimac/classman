package controllers;

import java.util.List;

import org.doctester.testbrowser.Request;
import org.doctester.testbrowser.Response;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;

import models.User;

public class UserControllerDocTest extends AuthenticatedDocTesterBase {
  private static final String USER_URL = "/api/admin/users";

  @Before
  @Override
  public void init() {
    makeRequest(Request.GET().url(testServerUrl().path("/db/reset?fixture=UserData")));
    super.init();
  }

  @Test
  public void getAllUsers() {
    sayNextSection("Retrieve all available stored Users.");

    say("Retrieving all available Users is a GET request to " + USER_URL);

    Response response = sayAndMakeRequest(
      Request.GET()
        .url(testServerUrl().path(USER_URL))
        .addHeader("X-AUTH-TOKEN", auth.auth_token)
      );

    List<User> users = response.payloadAs(List.class);

    sayAndAssertThat("All available Users are returned.",
        users.size(), CoreMatchers.is(1));
  }

  @Test
  public void getUser() {
    sayNextSection("Retrieve one User.");

    say("Retrieving one User is a GET request to " + USER_URL + "/[id]");

    Response response = sayAndMakeRequest(
      Request.GET()
        .url(testServerUrl().path(USER_URL + "/1"))
        .addHeader("X-AUTH-TOKEN", auth.auth_token)
      );

    User user = response.payloadAs(User.class);

    sayAndAssertThat("Requested User is returned.",
        user.getFirstName(), CoreMatchers.is("First"));
  }

  @Test
  public void createUser() {
    sayNextSection("Insert User.");

    say("Inserting a User is a POST request to " + USER_URL);

    User user = new User();
    user.setFirstName("Test");
    user.setLastName("McUser");

    Response response = sayAndMakeRequest(
      Request.POST()
        .url(testServerUrl().path(USER_URL))
        .contentTypeApplicationJson()
        .payload(user)
        .addHeader("X-AUTH-TOKEN", auth.auth_token)
      );

    User rUser = response.payloadAs(User.class);

    sayAndAssertThat("User is inserted.",
        rUser.getId(), CoreMatchers.notNullValue());
  }
  @Test
  public void updateUser() {
    sayNextSection("Update User.");

    say("Updating a User is a PUT request to " + USER_URL + "/[id]");


    User user = new User();
    user.setFirstName("John");
    user.setLastName("McUser");
    user.setEmail("jm@test.com");

    Response response = sayAndMakeRequest(
      Request.PUT()
        .url(testServerUrl().path(USER_URL + "/1"))
        .contentTypeApplicationJson()
        .payload(user)
        .addHeader("X-AUTH-TOKEN", auth.auth_token)
      );

    User rUser = response.payloadAs(User.class);

    sayAndAssertThat("Class is updated.",
        rUser.getFirstName(), CoreMatchers.is("John"));
  }

  @Test
  public void deleteUser() {
    sayNextSection("Delete User.");

    say("Deleting a User is a DELETE request to " + USER_URL + "/[id]");


    Response response = sayAndMakeRequest(
      Request.DELETE()
        .url(testServerUrl().path(USER_URL + "/1"))
        .addHeader("X-AUTH-TOKEN", auth.auth_token)
      );

    User user = response.payloadAs(User.class);

    sayAndAssertThat("User deleted returned.",
        user.getId(), CoreMatchers.is(1L));
  }
}
