/**
 * Copyright (C) 2016 AppDynamics
 *
 * @author William Dimaculangan
 */
package scheduled;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.eclipse.jetty.util.MultiMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.ec2.model.Instance;
import com.appdynamics.aws.AwsAdaptor;
import com.appdynamics.aws.AwsAdaptor.Region;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.jensfendler.ninjaquartz.annotations.QuartzSchedule;

import dao.SimpleDao;
  
/**
 * Scheduled job to check for AWS objects that have been removed
 * outside of the Class Manager application.
 * 
 * @author William Dimaculangan
 *
 */
@Singleton
public class AwsValidator {
  static final Logger log = LoggerFactory.getLogger(AwsValidator.class);

  @Inject
  SimpleDao<models.Instance> instDao;
  @Inject
  AwsAdaptor aws;
  @Inject
  Provider<EntityManager> entityManagerProvider;
  @Inject
  com.google.inject.persist.UnitOfWork unitOfWork;

  @QuartzSchedule(cronSchedule = "0 0 23 * * ?")
  public void checkAws() {
    unitOfWork.begin();
    EntityManager em = entityManagerProvider.get();
    EntityTransaction trans = em.getTransaction();
    if (!trans.isActive())
      trans.begin();
    
    log.info("AWS verification start.");
    try {
      checkInstances();
    } finally {
      if (trans.getRollbackOnly()) {
        trans.rollback();
      } else {
        trans.commit();
      }
      unitOfWork.end();
    }
    log.info("Aws verification ended.");

  }

  private void checkInstances() {
    List<models.Instance> instances = instDao.getAll(models.Instance.class);
    MultiMap<models.Instance> regionMap = new MultiMap<>();
    for (models.Instance instance: instances) {
      if (!instance.isTerminated()) {
        regionMap.put(instance.getRegion(), instance);
      }
    }
    
    for (String region: regionMap.keySet()) {
      List<Instance> existing = aws.getInstances(Region.valueOf(region));
      Set<String> ids = existing.stream()
          .filter((inst) -> { return inst.getState().getCode() != 48; })
          .map((inst)-> { return inst.getInstanceId(); })
          .collect(Collectors.toSet());
      for (models.Instance inst:instances) {
        if (!ids.contains(inst.getId())) {
          inst.setTerminated(true);
          instDao.update(inst);
          log.info("Instance marked terminated: " + inst.getId());
        }
      }
    }
  }
}
