package dao;

import javax.inject.Singleton;
import javax.persistence.EntityManager;

import com.google.inject.Inject;
import com.google.inject.Provider;

import models.Token;

@Singleton
public class TokenDao {
  @Inject
  Provider<EntityManager> entityManagerProvider;

  public void persist(Token token) {
    EntityManager entityManager = entityManagerProvider.get();
    entityManager.persist(token);
  }
}
