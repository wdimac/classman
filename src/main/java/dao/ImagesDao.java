package dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import models.Images;

@Singleton
public class ImagesDao {
  @Inject
  Provider<EntityManager> entityManagerProvider;

  public List<Images> getAllImages() {
    EntityManager entityManager = entityManagerProvider.get();

    TypedQuery<Images> query = entityManager.createQuery("Select i from Images i order by region, description	", Images.class);
    List<Images> images = query.getResultList();
    return images;
  }

  public void persist(Images image) {
    try {
      EntityManager entityManager = entityManagerProvider.get();

      entityManager.persist(image);
    } catch (Exception e) {
      e.printStackTrace();
      throw e;
    }
  }
}
