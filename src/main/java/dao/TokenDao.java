package dao;

import java.util.List;

import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.persist.Transactional;

import models.Token;
import ninja.jpa.UnitOfWork;

@Singleton
public class TokenDao {
  @Inject
  Provider<EntityManager> entityManagerProvider;

  public void persist(Token token) {
    EntityManager entityManager = entityManagerProvider.get();
    entityManager.persist(token);
  }

  @UnitOfWork
  public boolean isValidToken(Token token) {
    try {
    EntityManager entityManager = entityManagerProvider.get();

    TypedQuery<Token> query = entityManager.createQuery("Select t from Token t where t.token=:tokenParam", Token.class);
    List<Token> tokens = query.setParameter("tokenParam", token.getToken()).getResultList();

    return tokens.size() == 1;
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }
}
