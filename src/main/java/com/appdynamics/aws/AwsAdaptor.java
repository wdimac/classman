package com.appdynamics.aws;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.handlers.AsyncHandler;
import com.amazonaws.services.ec2.AmazonEC2AsyncClient;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.Address;
import com.amazonaws.services.ec2.model.AllocateAddressRequest;
import com.amazonaws.services.ec2.model.AllocateAddressResult;
import com.amazonaws.services.ec2.model.AmazonEC2Exception;
import com.amazonaws.services.ec2.model.AssociateAddressRequest;
import com.amazonaws.services.ec2.model.AssociateAddressResult;
import com.amazonaws.services.ec2.model.CreateTagsRequest;
import com.amazonaws.services.ec2.model.DescribeAddressesRequest;
import com.amazonaws.services.ec2.model.DescribeAddressesResult;
import com.amazonaws.services.ec2.model.DescribeImagesRequest;
import com.amazonaws.services.ec2.model.DescribeImagesResult;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.DescribeSecurityGroupsRequest;
import com.amazonaws.services.ec2.model.DescribeSecurityGroupsResult;
import com.amazonaws.services.ec2.model.DescribeSubnetsResult;
import com.amazonaws.services.ec2.model.DisassociateAddressRequest;
import com.amazonaws.services.ec2.model.DomainType;
import com.amazonaws.services.ec2.model.Filter;
import com.amazonaws.services.ec2.model.Image;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceState;
import com.amazonaws.services.ec2.model.InstanceStateChange;
import com.amazonaws.services.ec2.model.InstanceStateName;
import com.amazonaws.services.ec2.model.ReleaseAddressRequest;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.SecurityGroup;
import com.amazonaws.services.ec2.model.StartInstancesRequest;
import com.amazonaws.services.ec2.model.StartInstancesResult;
import com.amazonaws.services.ec2.model.StopInstancesRequest;
import com.amazonaws.services.ec2.model.StopInstancesResult;
import com.amazonaws.services.ec2.model.Subnet;
import com.amazonaws.services.ec2.model.Tag;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import dao.SimpleDao;
import models.Eip;

@Singleton
public class AwsAdaptor {
  /**
   * List of Regions we could connect to.
   *
   * Hard-coded here so we don't have to connect to a region
   * to find out what regions we can connect to.
   *
   * @author William Dimaculangan
   */
  public enum Region {
    US_EAST_1	      ("US East (N. Virginia)", "ec2.us-east-1.amazonaws.com"),
    US_WEST_2	      ("US West (Oregon)", "ec2.us-west-2.amazonaws.com"),
    US_WEST_1	      ("US West (N. California)", "ec2.us-west-1.amazonaws.com"),
    EU_WEST_1	      ("EU (Ireland)", "ec2.eu-west-1.amazonaws.com"),
    EU_CENTRAL_1	  ("EU (Frankfurt)", "ec2.eu-central-1.amazonaws.com"),
    AP_SOUTHEAST_1	("Asia Pacific (Singapore)", "ec2.ap-southeast-1.amazonaws.com"),
    AP_SOUTHEAST_2	("Asia Pacific (Sydney)", "ec2.ap-southeast-2.amazonaws.com"),
    AP_NORTHEAST_1	("Asia Pacific (Tokyo)", "ec2.ap-northeast-1.amazonaws.com"),
    SA_EAST_1	      ("South America (Sao Paulo)", "ec2.sa-east-1.amazonaws.com");

    private String name;
    private String endpoint;
    public String getName() {
      return name;
    }
    public String getEndpoint() {
      return endpoint;
    }

    Region(String name, String endpoint){
      this.name = name;
      this.endpoint = endpoint;
    }
    Region(){};

    public static ArrayList<String[]> getNameList() {

      ArrayList<String[]> nameList = new ArrayList<>();
      for (Region region: Region.values()){
        String[] foo = {region.toString(), region.getName()};
        nameList.add(foo);
      }
      return nameList;
    }
  }

  static final Logger log = LoggerFactory.getLogger(AwsAdaptor.class);
  @Inject
  private SimpleDao<Eip> eipDao;
  @Inject
  com.google.inject.persist.UnitOfWork unitOfWork;
  private static final Pattern INSTANCE_MISSING_ERROR_REGEX = Pattern.compile(".*The instance ID '(.*)' does not exist.*");
  private static final InstanceState TERMINATED_STATE = new InstanceState().withCode(48).withName(InstanceStateName.Terminated);

  /**
   * Lookup all security groups for the region.
   *
   * @param region
   * @return
   */
  public List<SecurityGroup> getSecurityGroups(Region region) {
    AmazonEC2Client amazonClient = getClient(region);
    DescribeSecurityGroupsResult groups = amazonClient.describeSecurityGroups();
    return groups.getSecurityGroups();
  }

  /**
   * Return all items listed
   * AmazonEC2Client amazonClient = getClient(region);
   * @param ids
   * @return
   */
  public List<SecurityGroup> getSecurityGroups(String[] ids, String region) {
    AmazonEC2Client amazonClient = getClient(Region.valueOf(region));
    DescribeSecurityGroupsResult result = amazonClient
        .describeSecurityGroups(
            new DescribeSecurityGroupsRequest().withGroupIds(ids));
    return result.getSecurityGroups();
  }

  /**
   * Return all items listed
   * AmazonEC2Client amazonClient = getClient(region);
   * @param ids
   * @return
   */
  public List<Subnet> getAllVpcs(Region region) {
    AmazonEC2Client amazonClient = getClient(region);
    DescribeSubnetsResult result = amazonClient.describeSubnets();
    return result.getSubnets();
  }

  /**
   * Finds all private Images available.
   *
   * @param region
   * @return
   */
  public List<Image> getImages(Region region) {
    AmazonEC2Client amazonClient = getClient(region);
    DescribeImagesResult result = amazonClient
        .describeImages(
            new DescribeImagesRequest().withFilters(
                new Filter("is-public").withValues("false")
            ));
    return result.getImages();
  }

  /**
   * Return all items listed
   * AmazonEC2Client amazonClient = getClient(region);
   * @param ids
   * @return
   */
  public List<Image> getImages(String[] ids, String region) {
    AmazonEC2Client amazonClient = getClient(Region.valueOf(region));
    DescribeImagesResult result = amazonClient
        .describeImages(
            new DescribeImagesRequest().withImageIds(ids));
    return result.getImages();
  }

  /**
   * Attempt to start the instances.
   *
   * @param region
   * @param request
   * @param name
   * @return
   */
  public List<Instance> runInstances(Region region, RunInstancesRequest request, String name) {
    AmazonEC2Client amazonClient = getClient(region);
    RunInstancesResult results = amazonClient.runInstances(request);
    List<Instance> instanceList = results.getReservation().getInstances();


    Tag tag = new Tag().withKey("Name").withValue(name);
    List<String> idList = new ArrayList<>();
    for (Instance inst: instanceList) {
      idList.add(inst.getInstanceId());
    }
    amazonClient.createTags(new CreateTagsRequest()
        .withResources(idList)
        .withTags(tag));

    return instanceList;
  }

  public List<String> startInstances(Region region, String[] ids){
    AmazonEC2Client amazonClient = getClient(region);
    StartInstancesRequest startInstancesRequest =
        new StartInstancesRequest()
            .withInstanceIds(ids);
    StartInstancesResult results = amazonClient.startInstances(startInstancesRequest );
    List<InstanceStateChange> list = results.getStartingInstances();
    List<String> result = new ArrayList<>();
    for (InstanceStateChange change: list) {
      result.add(change.getInstanceId());
    }
    List<Eip> eips = new ArrayList<>();

    for (String instanceId : ids) {
      Eip target = new Eip();
      models.Instance inst = new models.Instance();
      inst.setId(instanceId);
      target.setInstance(inst);
      target = eipDao.findBy(target);
      if (target != null && "standard".equals(target.getDomain())) {
        eips.add(target);
      }
    }
    if (eips.size() > 0)
      this.associateEipWhenReady(eips);

    return result;
  }

  public List<String> stopInstances(Region region, String[] ids){
    AmazonEC2Client amazonClient = getClient(region);
    StopInstancesRequest stopInstanceRequest =
        new StopInstancesRequest()
            .withInstanceIds(ids);
    StopInstancesResult results = amazonClient.stopInstances(stopInstanceRequest );
    List<InstanceStateChange> list = results.getStoppingInstances();
    List<String> result = new ArrayList<>();
    for (InstanceStateChange change: list) {
      result.add(change.getInstanceId());
    }
    return result;
  }

  public List<String> terminateInstances(Region region, String[] ids){
    AmazonEC2Client amazonClient = getClient(region);
    for(String id: ids) {

      TerminateInstancesRequest terminateInstancesRequest =
          new TerminateInstancesRequest()
              .withInstanceIds(new QuickList<String>(id));
      try {
        amazonClient.terminateInstances(terminateInstancesRequest );
      } catch (AmazonServiceException ex) {

        // Check the ErrorCode to see if the instance does not exist.
        if ("InvalidInstanceID.NotFound".equals(ex.getErrorCode())) {
          log.info("Instance " + id + " does not exist. Already terminated?");
        } else {
          // The exception was thrown for another reason, so re-throw the
          // exception.
          throw ex;
        }
      }
      cleanUpEips(id);
    }

    return Arrays.asList(ids);
  }

  public void cleanUpEips(String instanceId) {
    Eip qEip = new Eip();
    models.Instance inst = new models.Instance();
    inst.setId(instanceId);
    qEip.setInstance(inst);
    Eip eip = eipDao.findBy(qEip);
    if (eip != null) {
      this.disassociateEip(eip);
      if (eip.getPoolUser() == null) {
        this.releaseEips(eip.getRegion(), eip.getAllocationId(), eip.getPublicIp());
        eipDao.delete(eip.getId(), Eip.class);
      } else {
        eip.setInstance(null);
        eipDao.persist(eip);
      }
    }
  }

  /**
   * Return all instances in a region.
   * 
   * @param region
   * @return
   */
  public List<Instance> getInstances(Region region) {
    AmazonEC2Client amazonClient = getClient(region);
    DescribeInstancesResult result = amazonClient.describeInstances();
    List<Reservation> reservations = result.getReservations();
    return reservations.stream()
      .flatMap((res) ->{return res.getInstances().stream();})
      .collect(Collectors.toList());
  }

  /**
   * Return all items listed
   * AmazonEC2Client amazonClient = getClient(region);
   * @param ids
   * @return
   */
  public List<Instance> getInstances(List<String> ids, String region) {
    AmazonEC2Client amazonClient = getClient(Region.valueOf(region));
    List<String> badIds = new ArrayList<>();
    boolean working = true;
    DescribeInstancesResult result = null;
    while (working) {
      try {
        result = amazonClient
            .describeInstances(
                new DescribeInstancesRequest().withInstanceIds(ids));
        working = false;
      } catch (AmazonEC2Exception e) {
        Matcher m = INSTANCE_MISSING_ERROR_REGEX.matcher(e.getMessage());
        if (m.matches()) {
          String id = m.group(1);
          badIds.add(id);
          ids.remove(id);
        } else {
          throw e;
        }
      }
    }
    List<Reservation> reserves = result.getReservations();
    List<Instance> instances = new ArrayList<>();
    for (Reservation res: reserves) {
      instances.addAll(res.getInstances());
    }
    for (String id: badIds) {
      Instance terminated = 
          new Instance()
          .withInstanceId(id)
          .withState(TERMINATED_STATE);
      instances.add(terminated);
    }
    
    return instances;
  }

  /**
   * Get list of EIP addresses for a region
   *
   * @param region
   * @return
   */
  public List<Address> getEips(String region) {
    AmazonEC2Client amazonClient = getClient(Region.valueOf(region));
    DescribeAddressesResult addrs = amazonClient.describeAddresses();
    return addrs.getAddresses();
  }

  /**
   * Request details for specific addresses.
   *
   * @param region
   * @param publicIps
   * @return
   */
  public List<Address> getEips(String region, List<String> publicIps) {
    AmazonEC2Client amazonClient = getClient(Region.valueOf(region));
    DescribeAddressesResult addrs = amazonClient.describeAddresses(
        new DescribeAddressesRequest()
        .withPublicIps(publicIps));
    return addrs.getAddresses();
  }

  /**
   * Get association ID.
   *
   * @param region
   * @param allocId
   * @param publicIp
   * @return
   */
  public String associateEip(String region, String allocId, String publicIp, String instanceId) {
    AmazonEC2Client amazonClient = getClient(Region.valueOf(region));
    AssociateAddressRequest request = new AssociateAddressRequest().withInstanceId(instanceId);
    if (allocId != null) {
      request.withAllocationId(allocId);
    } else {
      request.withPublicIp(publicIp);
    }
    AssociateAddressResult result = amazonClient.associateAddress(request);
    return result.getAssociationId();
  }

  /**
   * Get association ID.
   *
   * @param region
   * @param allocId
   * @param publicIp
   * @return
   */
  public void associateEipWhenReady(List<Eip> eips) {
    if (eips.isEmpty()) return;

    AmazonEC2AsyncClient amazonClient = getAsyncClient(Region.valueOf(eips.get(0).getRegion()));
    AsyncHandler<DescribeInstancesRequest, DescribeInstancesResult> asyncHandler = new InstanceStartWaitHandler(eips, this);
    List<String> instanceIds = new ArrayList<>();
    for (Eip eip: eips) {
      if (eip.getInstance() != null)
        instanceIds.add(eip.getInstance().getId());
    }
    DescribeInstancesRequest describeInstancesRequest =
        new DescribeInstancesRequest()
        .withInstanceIds(instanceIds);
    amazonClient.describeInstancesAsync(describeInstancesRequest, asyncHandler);
  }

  public void disassociateEip(Eip eip) {
    AmazonEC2Client amazonClient = getClient(Region.valueOf(eip.getRegion()));
    DisassociateAddressRequest request = new DisassociateAddressRequest();
    if (eip.getAllocationId() != null)
      request.setAssociationId(eip.getAssociationId());
    else
      request.setPublicIp(eip.getPublicIp());
    try {
      amazonClient.disassociateAddress(request);
    } catch (AmazonEC2Exception ae) {
      if (ae.getStatusCode() == 400) {
        log.info("no association, ignoring");
      } else {
        log.error("Failed to dissociate EIP", ae);
      }
    }
  }

  /**
   * Request allocation of new Eip.
   *
   * @param region
   * @param is_vpc
   * @return
   */
  public String requestEip(String region, boolean is_vpc) {
    AmazonEC2Client amazonClient = getClient(Region.valueOf(region));
    AllocateAddressResult result = amazonClient.allocateAddress(
        new AllocateAddressRequest()
        .withDomain(is_vpc ? DomainType.Vpc: DomainType.Standard));
    return result.getPublicIp();
  }

  /**
   * Release allocated Eip.
   *
   * @param region
   * @param allocId
   * @param publicIp
   */
  public void releaseEips(String region, String allocId, String publicIp) {
    AmazonEC2Client amazonClient = getClient(Region.valueOf(region));
    ReleaseAddressRequest request = new ReleaseAddressRequest();
    if (allocId != null) {
      request.withAllocationId(allocId);
    } else {
      request.withPublicIp(publicIp);
    }
    try {
      amazonClient.releaseAddress(request);
    } catch (AmazonEC2Exception ae) {
      if (ae.getStatusCode() == 400) {
        log.info("EIP " + publicIp + " has already been released.");
      } else {
        log.error("Failed to release EIP: " + publicIp + " cause: " + ae.getErrorMessage());
      }
    }
  }

  /**
   * Private method to establish a client connection.
   *
   * @param region
   * @return
   */
   AmazonEC2Client getClient(Region region) {
    AmazonEC2Client amazonEC2Client = new AmazonEC2Client();
    amazonEC2Client.setEndpoint(region.getEndpoint());
    return amazonEC2Client;
  }

  /**
   * Private method to establish a client connection.
   *
   * @param region
   * @return
   */
   static AmazonEC2AsyncClient getAsyncClient(Region region) {
    AmazonEC2AsyncClient amazonEC2Client = new AmazonEC2AsyncClient();
    amazonEC2Client.setEndpoint(region.getEndpoint());
    return amazonEC2Client;
  }

  public static class InstanceStartWaitHandler implements AsyncHandler<DescribeInstancesRequest, DescribeInstancesResult> {
    static final Logger log = LoggerFactory.getLogger(InstanceStartWaitHandler.class);
    private List<Eip> eips;
    private AwsAdaptor aws;

    public InstanceStartWaitHandler(List<Eip> eips, AwsAdaptor aws) {
      this.eips = eips;
      this.aws  = aws;
    }

    @Override
    public void onError(Exception ex) {
      log.error("Exception checking instance state.", ex);
    }

    @Override
    public void onSuccess(DescribeInstancesRequest request, DescribeInstancesResult result) {
      for (Reservation res: result.getReservations()) {
        for (Instance inst :res.getInstances()) {
          if (inst.getState().getName().equals("running")) {
            for (Eip eip: eips) {
              if (inst.getInstanceId().equals(eip.getInstance().getId())) {
                log.debug("Associating: " + eip.getPublicIp() + " to instance " + inst.getInstanceId());
                String assocId = aws.associateEip(eip.getRegion(),
                                 eip.getAllocationId(),
                                 eip.getPublicIp(),
                                 eip.getInstance().getId());
                eips.remove(eip);
                eip.setAssociationId(assocId);
                aws.updateDuringAsync(eip);
                request.getInstanceIds().remove(eip.getInstance().getId());
              }
            }
          } else if (!inst.getState().getName().equals("pending")) {
            log.error(MessageFormat.format("Instance not starting, abandoning wait: Instance: %s State: %s",
                inst.getInstanceId(), inst.getState().getName()));
            for (Eip eip: eips) {
              if (inst.getInstanceId().equals(eip.getInstance().getId())) {
                eips.remove(eip);
                request.getInstanceIds().remove(eip.getInstance().getId());
              }
            }
          }
        }
      }

      if (!eips.isEmpty()) {
        AmazonEC2AsyncClient amazonClient = getAsyncClient(Region.valueOf(eips.get(0).getRegion()));
        amazonClient.describeInstancesAsync(request, this);
      }
    }
  }

  /**
   * Method to update an eip during async handler.
   *
   * @param eip
   */
  public void updateDuringAsync(Eip eip) {
    unitOfWork.begin();
    eipDao.beginTransaction();
    try {
      eipDao.update(eip);
      log.info("Association:" + eip.getAssociationId());
    } finally {
      eipDao.commitTrans();
      unitOfWork.end();
    }
  }
}
