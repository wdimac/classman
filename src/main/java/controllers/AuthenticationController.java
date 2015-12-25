package controllers;

import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.inject.persist.Transactional;

import dao.TokenDao;
import models.Token;
import ninja.Result;
import ninja.Results;
import ninja.jaxy.POST;
import ninja.jaxy.Path;
import ninja.params.Param;

@Path("/api")
@Singleton
public class AuthenticationController {
  @Inject
  TokenDao tokenDao;

  /**
   * Validates username and password. Returns a token that the client
   * can use for subsequent requests.
   *
   * TODO: Implement User database
   * TODO: use Json Web Tokens?
   *
   * @param username
   * @param password
   * @return json reply containing token
   */
  @Path("/authenticate")
  @POST
  @Transactional
  public Result authenticate(@Param("username")String username,
      @Param("password")String password) {
    if ("admin".equals(username) && "admin".equals(password)) {
      Token token = new Token();
      token.setToken(UUID.randomUUID().toString());
      tokenDao.persist(token);

      return Results.json().render("auth_token",token.getToken());
    } else {
      return Results.unauthorized().json();
    }
  }

  /**
   * Class for parsing response.
   *
   * Used primarily in testing.
   */
  public static class AuthenicationResponse {
    public String auth_token;

  }
}
