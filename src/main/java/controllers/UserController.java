package controllers;

import javax.inject.Singleton;

import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

import dao.SimpleDao;
import filters.TokenFilter;
import models.User;
import ninja.FilterWith;
import ninja.Result;
import ninja.Results;
import ninja.jaxy.DELETE;
import ninja.jaxy.GET;
import ninja.jaxy.POST;
import ninja.jaxy.PUT;
import ninja.jaxy.Path;
import ninja.params.PathParam;

@Path("/api/admin")
@FilterWith(TokenFilter.class)
@Singleton
public class UserController {
  @Inject
  SimpleDao<User> userDao;

  /**
   * Get list of users
   * @return
   */
  @Path("/users")
  @GET
  @Transactional
  public Result getUsers() {
    userDao.clearSession();
    return Results.json().render(userDao.getAll(models.User.class));
  }

  /**
   * Get single user
   * @return
   */
  @Path("/users/{id}")
  @GET
  @Transactional
  public Result getUser(@PathParam("id") Long id) {
    return Results.json().render(userDao.find(id, models.User.class));
  }

  /**
   * Insert new user information.
   *
   * @param user
   * @return
   */
  @Path("/users")
  @POST
  @Transactional
  public Result addUser(models.User user) {

    userDao.persist(user);

    return Results.json().render(user);
  }

  @Path("/users/{id}")
  @DELETE
  @Transactional
  public Result deleteUser(@PathParam("id") String id) {
    User user = userDao.delete(Long.valueOf(id), User.class);

    return Results.json().render(user);
  }

  /**
   * Update user information.
   *
   * @param user
   * @return
   */
  @Path("/users/{id}")
  @PUT
  @Transactional
  public Result updateUser(@PathParam("id") String id, User user) {
    user.setId(Long.valueOf(id));
    userDao.update(user);

    return Results.json().render(user);
  }

}
