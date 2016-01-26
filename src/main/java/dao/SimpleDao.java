package dao;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.SingularAttribute;

import org.hibernate.Session;

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

    Metamodel m = entityManager.getMetamodel();
    EntityType<M> itemType = m.entity(clazz);
    Set<SingularAttribute<M, ?>> attrs = itemType.getDeclaredSingularAttributes();
    List<Order> oList = new ArrayList<>();
    for (SingularAttribute<M, ?> attr: attrs) {
      if (attr.getName().equals("region")) {
        oList.add(cb.asc(root.get(attr)));
      }
      if (attr.getName().equals("description")) {
        oList.add(cb.asc(root.get(attr)));
      }
    }

    if (oList.size() > 0)
      cq.orderBy(oList);

    TypedQuery<M> query=entityManager.createQuery(cq);
    List<M> items = query.getResultList();
    return items;
  }

  public void persist(M item) {
    try {
      EntityManager entityManager = entityManagerProvider.get();

      entityManager.persist(item);
      entityManager.flush();
    } catch (Exception e) {
      e.printStackTrace();
      throw e;
    }
  }

  public void update(M item) {
    EntityManager entityManager = entityManagerProvider.get();
    item = entityManager.merge(item);
    entityManager.persist(item);
  }

  public M delete(Object id, Class<M> clazz) {
    EntityManager entityManager = entityManagerProvider.get();
    M item = entityManager.find(clazz, id);
    entityManager.remove(item);
    return item;
  }

  public M find(Object id, Class<M> clazz) {
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
    M item=null;
    try {
      item = query.getSingleResult();
    } catch (NoResultException e) {
      //return null
    }
    return item;
  }

  public M findBy(M item){
    EntityManager entityManager = entityManagerProvider.get();
    Class<M> clazz = (Class<M>)item.getClass();
    CriteriaBuilder cb = entityManager.getCriteriaBuilder();
    CriteriaQuery<M> cq = cb.createQuery(clazz);
    Root<M> root = cq.from(clazz);
    cq.select(root);

    Expression<Boolean> e = null;
    for (Method meth:item.getClass().getMethods()){
      if (meth.getName().startsWith("get")) {
        String attrName = meth.getName();
        attrName = attrName.substring(0, 4).substring(3).toLowerCase() + attrName.substring(4);
        if ("class".equals(attrName))
          continue;

        Object value;
        try {
          value = meth.invoke(item);
          if (value != null) {

            if (e == null) {
              e = cb.equal(root.get(attrName), value);
            } else {
              e = cb.and(cb.equal(root.get(attrName), value), e);
            }
          }
        } catch (Exception ex) {
          ex.printStackTrace();
        }
      }
    }
    cq.where(e);
    TypedQuery<M> query = entityManager.createQuery(cq);
    M result = null;
    try {
      result = query.getSingleResult();
    } catch (NoResultException nre) {
      // Just return null
    }
    return result;
  }

  public void clearSession() {
    EntityManager entityManager = entityManagerProvider.get();
    Session session = entityManager.unwrap(Session.class);
    session.clear();
  }
}
