package dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import models.Images;
import models.Instance;

@Singleton
public class InstanceDao {
  @Inject
  Provider<EntityManager> entityManagerProvider;

  public List<Instance> getAllInstances() {
    EntityManager entityManager = entityManagerProvider.get();

    TypedQuery<Instance> query = entityManager.createQuery("Select i from Instance i order by region, description	", Instance.class);
    List<Instance> images = query.getResultList();
    return images;
  }

  public void persist(Instance instance) {
    try {
      EntityManager entityManager = entityManagerProvider.get();

      entityManager.persist(instance);
    } catch (Exception e) {
      e.printStackTrace();
      throw e;
    }
  }

  public Instance delete(String instanceId) {
    try {
      EntityManager entityManager = entityManagerProvider.get();
      Instance instance = entityManager.find(Instance.class, instanceId);
      entityManager.remove(instance);
      return instance;
    } catch (Exception e) {
      e.printStackTrace();
      throw e;
    }
  }

  public Instance find(String id) {
    EntityManager entityManager = entityManagerProvider.get();

    TypedQuery<Instance> query = entityManager.createQuery("Select i from Instance i where i.id=:idParam", Instance.class);
    Instance image = query.setParameter("idParam", id).getSingleResult();
    return image;
  }

}
