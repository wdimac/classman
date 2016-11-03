package scheduled;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.amazonaws.services.ec2.model.Address;
import com.amazonaws.services.ec2.model.Image;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceState;
import com.amazonaws.services.ec2.model.Subnet;
import com.appdynamics.aws.AwsAdaptor;
import com.appdynamics.aws.AwsAdaptor.Region;
import com.appdynamics.aws.QuickList;
import com.google.inject.Provider;
import com.google.inject.persist.UnitOfWork;

import dao.SimpleDao;
import models.Eip;
import models.Images;
import models.SecurityGroup;
import models.Vpc;

@RunWith(MockitoJUnitRunner.class)
public class AwsValidatorTest {

  AwsValidator av;
  @Mock
  AwsAdaptor aws;
  @Mock
  SimpleDao<models.Instance> instDao;
  @Mock
  SimpleDao<models.Eip> eipDao;
  @Mock
  SimpleDao<models.Images> imageDao;
  @Mock
  SimpleDao<models.SecurityGroup> groupDao;
  @Mock
  SimpleDao<models.Vpc> vpcDao;
  @Mock
  Provider<EntityManager> entityManagerProvider;
  @Mock
  EntityManager em;
  @Mock
  EntityTransaction trans;
  @Mock
  UnitOfWork unitOfWork;
  
  @Before
  public void init() {
    av = new AwsValidator();
    av.aws = aws;
    av.instDao = instDao;
    av.eipDao = eipDao;
    av.imageDao = imageDao;
    av.groupDao = groupDao;
    av.vpcDao = vpcDao;
    av.entityManagerProvider = entityManagerProvider;
    av.unitOfWork = unitOfWork;
    when(entityManagerProvider.get()).thenReturn(em);
    when(em.getTransaction()).thenReturn(trans);
  }
  
  @Test
  public void testInstanceValidation() {
    models.Instance inst1 = new models.Instance();
    inst1.setId("inst1");
    inst1.setRegion(Region.US_WEST_2.toString());
    models.Instance inst2 = new models.Instance();
    inst2.setId("inst2");
    inst2.setRegion(Region.US_WEST_2.toString());
    List<models.Instance> instances = new QuickList<models.Instance>(inst1, inst2);
    when(instDao.getAll(models.Instance.class)).thenReturn(instances);
    
    when(aws.getInstances(any(Region.class))).thenReturn(new QuickList<Instance>(
      new Instance()
        .withInstanceId("inst1")
        .withState(new InstanceState().withCode(16))));
    
    av.checkAws();
    
    assertFalse(instances.get(0).isTerminated());
    assertTrue(instances.get(1).isTerminated());
  }
  
  @Test
  public void testEipValidation() {
    Eip eip1 = new Eip();
    eip1.setPublicIp("0.0.0.0");
    eip1.setRegion(Region.US_WEST_2.toString());
    Eip eip2 = new Eip();
    eip2.setPublicIp("0.0.0.1");
    eip2.setRegion(Region.US_WEST_2.toString());
    List<Eip> eips = new QuickList<>(eip1, eip2);
    when(eipDao.getAll(Eip.class)).thenReturn(eips);
    
    when(aws.getEips(anyString())).thenReturn(new QuickList<Address>(
        new Address().withPublicIp("0.0.0.0")));
    
    av.checkAws();
    
    assertFalse(eips.get(0).isDefunct());
    assertTrue(eips.get(1).isDefunct());
  }

  @Test
  public void testImageValidation() {
    Images image1 = new Images();
    image1.setId("id1");
    image1.setRegion(Region.US_WEST_2.toString());
    Images image2 = new Images();
    image2.setId("id2");
    image2.setRegion(Region.US_WEST_2.toString());
    List<Images> images = new QuickList<>(image1, image2);
    when(imageDao.getAll(Images.class)).thenReturn(images);
    
    when(aws.getImages(any(Region.class))).thenReturn(new QuickList<Image>(
        new Image().withImageId("id1")));
    
    av.checkAws();
    
    assertFalse(images.get(0).isDefunct());
    assertTrue(images.get(1).isDefunct());
  }

  @Test
  public void testSecurityGroupValidation() {
    SecurityGroup group1 = new SecurityGroup();
    group1.setId("id1");
    group1.setRegion(Region.US_WEST_2.toString());
    SecurityGroup group2 = new SecurityGroup();
    group2.setId("id2");
    group2.setRegion(Region.US_WEST_2.toString());
    List<SecurityGroup> groups = new QuickList<>(group1, group2);
    when(groupDao.getAll(SecurityGroup.class)).thenReturn(groups);
    
    when(aws.getSecurityGroups(any(Region.class))).thenReturn(new QuickList<com.amazonaws.services.ec2.model.SecurityGroup>(
        new com.amazonaws.services.ec2.model.SecurityGroup().withGroupId("id1")));
    
    av.checkAws();
    
    assertFalse(groups.get(0).isDefunct());
    assertTrue(groups.get(1).isDefunct());
  }

  @Test
  public void testVpcValidation() {
    Vpc vpc1 = new Vpc();
    vpc1.setSubnetId("id1");
    vpc1.setRegion(Region.US_WEST_2.toString());
    Vpc vpc2 = new Vpc();
    vpc2.setSubnetId("id2");
    vpc2.setRegion(Region.US_WEST_2.toString());
    List<Vpc> vpcs = new QuickList<>(vpc1, vpc2);
    when(vpcDao.getAll(Vpc.class)).thenReturn(vpcs);
    
    when(aws.getAllVpcs(any(Region.class))).thenReturn(new QuickList<Subnet>(
        new Subnet().withSubnetId("id1")));
    
    av.checkAws();
    
    assertFalse(vpcs.get(0).isDefunct());
    assertTrue(vpcs.get(1).isDefunct());
  }
}
