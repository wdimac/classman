package dao;

import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.SingularAttribute;

import com.appdynamics.aws.QuickList;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

@Singleton
public class SimpleDao<M> {
  @Inject
  Provider<EntityManager> entityManagerProvider;

  public List<M> getAll(Class<M> clazz) {
    EntityManager entityManager = entityManagerProvider.get();

    CriteriaBuilder cb = entityManager.getCriteriaBuilder();
    CriteriaQuery<M> cq = cb.createQuery(clazz);
    Root<M> root = cq.from(clazz);
    cq.select(root);

    Order regOrder = null;
    Order descOrder = null;
    Metamodel m = entityManager.getMetamodel();
    EntityType<M> itemType = m.entity(clazz);
    Set<SingularAttribute<M, ?>> attrs = itemType.getDeclaredSingularAttributes();
    for (SingularAttribute<M, ?> attr: attrs) {
      if (attr.getName().equals("region")) {
        regOrder = cb.asc(root.get(attr));
      }
      if (attr.getName().equals("description")) {
        descOrder = cb.asc(root.get(attr));
      }
    }

    if (regOrder != null)
      cq.orderBy(new QuickList<Order>(regOrder, descOrder));

    TypedQuery<M> query=entityManager.createQuery(cq);
    List<M> items = query.getResultList();
    return items;
  }

  public void persist(M item) {
    try {
      EntityManager entityManager = entityManagerProvider.get();

      entityManager.persist(item);
    } catch (Exception e) {
      e.printStackTrace();
      throw e;
    }
  }

  public M delete(String id, Class<M> clazz) {
    EntityManager entityManager = entityManagerProvider.get();
    M item = entityManager.find(clazz, id);
    entityManager.remove(item);
    return item;
  }

  public M find(String id, Class<M> clazz) {
    EntityManager entityManager = entityManagerProvider.get();

    CriteriaBuilder cb = entityManager.getCriteriaBuilder();
    CriteriaQuery<M> cq = cb.createQuery(clazz);
    Root<M> root = cq.from(clazz);
    cq.select(root);

    Metamodel m = entityManager.getMetamodel();
    EntityType<M> itemType = m.entity(clazz);
    SingularAttribute<? super M, ?> idAttr = itemType.getSingularAttribute("id");
    cq.where(cb.equal(root.get(idAttr), id));
    TypedQuery<M> query = entityManager.createQuery(cq);
    M item = query.getSingleResult();
    return item;
  }
}