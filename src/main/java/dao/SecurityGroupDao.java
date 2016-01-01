package dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import com.google.inject.Inject;
import com.google.inject.Provider;

import models.SecurityGroup;

public class SecurityGroupDao {
  @Inject
  Provider<EntityManager> entityManagerProvider;

  public Object getAllGroups() {
    EntityManager entityManager = entityManagerProvider.get();

    TypedQuery<SecurityGroup> query = entityManager.createQuery("Select i from SecurityGroup i order by region, description	", SecurityGroup.class);
    List<SecurityGroup> groups = query.getResultList();
    return groups;
  }

  public void persist(SecurityGroup securityGroup) {
    try {
      EntityManager entityManager = entityManagerProvider.get();

      entityManager.persist(securityGroup);
    } catch (Exception e) {
      e.printStackTrace();
      throw e;
    }
  }

  public SecurityGroup delete(String securityGroupId) {
    try {
      EntityManager entityManager = entityManagerProvider.get();
      SecurityGroup securityGroup = entityManager.find(SecurityGroup.class, securityGroupId);
      entityManager.remove(securityGroup);
      return securityGroup;
    } catch (Exception e) {
      e.printStackTrace();
      throw e;
    }
  }

  public SecurityGroup find(String id) {
    EntityManager entityManager = entityManagerProvider.get();

    TypedQuery<SecurityGroup> query = entityManager.createQuery("Select i from securityGroups i where i.id=:idParam", SecurityGroup.class);
    SecurityGroup securityGroup = query.setParameter("idParam", id).getSingleResult();
    return securityGroup;
  }

}
