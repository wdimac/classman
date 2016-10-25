package scheduled;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.appdynamics.aws.AwsAdaptor.Region;
import com.google.inject.Provider;
import com.google.inject.persist.UnitOfWork;
import com.appdynamics.aws.AwsAdaptor;
import com.appdynamics.aws.QuickList;

import dao.SimpleDao;
import com.amazonaws.services.ec2.model.Instance;

@RunWith(MockitoJUnitRunner.class)
public class AwsValidatorTest {

  AwsValidator av;
  @Mock
  AwsAdaptor aws;
  @Mock
  SimpleDao<models.Instance> instDao;
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
    av.entityManagerProvider = entityManagerProvider;
    av.unitOfWork = unitOfWork;
    when(entityManagerProvider.get()).thenReturn(em);
    when(em.getTransaction()).thenReturn(trans);
  }
  
  @Test
  public void testValidation() {
    List<models.Instance> instances = getInstances();
    when(instDao.getAll(models.Instance.class)).thenReturn(instances);
    when(aws.getInstances(any(Region.class))).thenReturn(getAwsInstances());
    
    av.checkAws();
    
    assertFalse(instances.get(0).isTerminated());
    assertTrue(instances.get(1).isTerminated());
  }

  private List<Instance> getAwsInstances() {
    return new QuickList<Instance>(new Instance().withInstanceId("inst1"));
  }

  private List<models.Instance> getInstances() {
    models.Instance inst1 = new models.Instance();
    inst1.setId("inst1");
    inst1.setRegion(Region.US_WEST_2.toString());
    models.Instance inst2 = new models.Instance();
    inst2.setId("inst2");
    inst2.setRegion(Region.US_WEST_2.toString());
    return new QuickList<models.Instance>(inst1, inst2);
  }
}
