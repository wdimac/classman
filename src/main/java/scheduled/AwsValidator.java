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

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.ec2.model.Address;
import com.amazonaws.services.ec2.model.Image;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Subnet;
import com.appdynamics.aws.AwsAdaptor;
import com.appdynamics.aws.AwsAdaptor.Region;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.jensfendler.ninjaquartz.annotations.QuartzSchedule;

import dao.SimpleDao;
import models.Eip;
import models.Images;
import models.SecurityGroup;
import models.Vpc;
  
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
  SimpleDao<models.Eip> eipDao;
  @Inject
  SimpleDao<models.Images> imageDao;
  @Inject
  SimpleDao<models.SecurityGroup> groupDao;
  @Inject
  SimpleDao<models.Vpc> vpcDao;
  @Inject
  AwsAdaptor aws;
  @Inject
  Provider<EntityManager> entityManagerProvider;
  @Inject
  com.google.inject.persist.UnitOfWork unitOfWork;

  @QuartzSchedule(cronSchedule = "0 1/15 * * * ?")
  public void checkAws() {
    unitOfWork.begin();
    EntityManager em = entityManagerProvider.get();
    EntityTransaction trans = em.getTransaction();
    if (!trans.isActive())
      trans.begin();
    
    log.info("AWS verification start.");
    try {
      checkInstances();
      checkEips();
      checkImages();
      checkSecurityGroups();
      checkVpc();
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

  private void checkVpc() {
    List<Vpc> vpcs = vpcDao.getAll(Vpc.class);
    Multimap<String, Vpc> regionMap = HashMultimap.create();
    vpcs.stream()
      .filter((vpc) -> { return !vpc.isDefunct(); })
      .forEach((vpc) -> { regionMap.put(vpc.getRegion(), vpc); });
    
    log.info("Checking " + regionMap.keySet());
    regionMap.keySet().stream().forEach((region) -> {
      List<Subnet> subnets = aws.getAllVpcs(Region.valueOf(region));
      
      Set<String> subnetIds = subnets.stream()
          .map((subnet) -> { return subnet.getSubnetId(); })
          .collect(Collectors.toSet());
      
      regionMap.get(region).stream()
      .filter((vpc) -> { log.info(vpc.getSubnetId() + " " + subnetIds.contains(vpc.getSubnetId()));return !subnetIds.contains(vpc.getSubnetId()); })
      .forEach((vpc) -> {
        vpc.setDefunct(true);
        vpcDao.update(vpc);
        log.info("Vpc marked defunct: " + vpc.getVpcId() + ":" + vpc.getSubnetId());
      });
      
    });
  }

  private void checkSecurityGroups() {
    List<SecurityGroup> groups = groupDao.getAll(SecurityGroup.class);
    Multimap<String, SecurityGroup> regionMap = HashMultimap.create();
    groups.stream()
      .filter((group) -> { return !group.isDefunct(); })
      .forEach((group) -> { regionMap.put(group.getRegion(), group); });
    
    regionMap.keySet().stream().forEach((region) -> {
      List<com.amazonaws.services.ec2.model.SecurityGroup> awsGroups = aws.getSecurityGroups((Region.valueOf(region)));
      
      Set<String> groupIds = awsGroups.stream()
          .map((group) -> { return group.getGroupId(); })
          .collect(Collectors.toSet());
      
      regionMap.get(region).stream()
      .filter((group) -> { return !groupIds.contains(group.getId()); })
      .forEach((group) -> {
        group.setDefunct(true);
        groupDao.update(group);
        log.info("Security Group marked defunct: " + group.getId());
      });
      
    });
  }

  private void checkImages() {
    List<Images> images = imageDao.getAll(Images.class);
    Multimap<String, Images> regionMap = HashMultimap.create();
    images.stream()
      .filter((image) -> { return !image.isDefunct(); })
      .forEach((image) -> { regionMap.put(image.getRegion(), image); });
    
    regionMap.keySet().stream().forEach((region) -> {
      List<Image> addresses = aws.getImages(Region.valueOf(region));
      
      Set<String> imageIds = addresses.stream()
          .map((image) -> { return image.getImageId(); })
          .collect(Collectors.toSet());
      
      regionMap.get(region).stream()
      .filter((image) -> { return !imageIds.contains(image.getId()); })
      .forEach((image) -> {
        image.setDefunct(true);
        imageDao.update(image);
        log.info("Image marked defunct: " + image.getId());
      });
      
    });
  }

  private void checkEips() {
    List<Eip> eips = eipDao.getAll(Eip.class);
    Multimap<String, Eip> regionMap = HashMultimap.create();
    eips.stream()
      .filter((eip) -> { return !eip.isDefunct(); })
      .forEach((eip) -> { regionMap.put(eip.getRegion(), eip); });
    
    regionMap.keySet().stream().forEach((region) -> {
      List<Address> addresses = aws.getEips(region);
      
      Set<String> ips = addresses.stream()
          .map((ip) -> { return ip.getPublicIp(); })
          .collect(Collectors.toSet());
      
      regionMap.get(region).stream()
      .filter((eip) -> { return !ips.contains(eip.getPublicIp()); })
      .forEach((eip) -> {
        eip.setDefunct(true);
        eipDao.update(eip);
        log.info("Eip marked defunct: " + eip.getPublicIp());
      });
      
    });
  }

  private void checkInstances() {
    List<models.Instance> instances = instDao.getAll(models.Instance.class);
    Multimap<String, models.Instance> regionMap = HashMultimap.create();
    instances.stream()
      .filter((instance) -> { return !instance.isTerminated(); })
      .forEach((instance) -> { regionMap.put(instance.getRegion(), instance); });
    
    regionMap.keySet().stream().forEach((region) -> {
      List<Instance> existing = aws.getInstances(Region.valueOf(region));
      
      Set<String> ids = existing.stream()
          .filter((inst) -> { return inst.getState().getCode() != 48; })
          .map((inst)-> { return inst.getInstanceId(); })
          .collect(Collectors.toSet());
      
      regionMap.get(region).stream()
        .filter((inst) -> { return !ids.contains(inst.getId()); })
        .forEach((inst) -> {
          inst.setTerminated(true);
          instDao.update(inst);
          log.info("Instance marked terminated: " + inst.getId());
        });
    });
  }
}
