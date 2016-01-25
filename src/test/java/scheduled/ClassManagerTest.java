package scheduled;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Date;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.appdynamics.aws.AwsAdaptor;
import com.appdynamics.aws.AwsAdaptor.Region;
import com.appdynamics.aws.QuickList;

import controllers.ScheduledClassesController;
import dao.SimpleDao;
import models.ClassTypeDetail;
import models.Instance;
import models.ScheduledClass;

@RunWith(MockitoJUnitRunner.class)
public class ClassManagerTest {
  static final Logger log = LoggerFactory.getLogger(ClassManagerTest.class);

  ClassManager cm;
  @Mock
  ScheduledClassesController cont;
  @Mock
  AwsAdaptor aws;
  @Mock
  SimpleDao<ScheduledClass> cDao;
  @Mock
  SimpleDao<Instance> iDao;

  @Before
  public void init() {
    cm = new ClassManager();
    cm.aws = aws;
    cm.classDao = cDao;
    cm.instDao = iDao;
    cm.scController = cont;
  }

  @Test
  public void startInstances() {
    log.info("Testing start");
    List<ScheduledClass> classList = getClassStartsIn(+1, 0);
    when(cDao.getAll(ScheduledClass.class)).thenReturn(classList);
    cm.controlInstances();

    verify(cont).startClassInstances(0, classList.get(0));
  }

  @Test
  public void stopInstances() {
    log.info("Testing stop");
    List<ScheduledClass> classList = getClassStartsIn(-9, 0);
    when(cDao.getAll(ScheduledClass.class)).thenReturn(classList);
    cm.controlInstances();

    verify(aws).stopInstances(any(Region.class), any(String[].class));
  }

  @Test
  public void restartInstances() {
    log.info("Testing restart");
    List<ScheduledClass> classList = getClassStartsIn(+1, 2);
    when(cDao.getAll(ScheduledClass.class)).thenReturn(classList);
    cm.controlInstances();

    verify(aws).startInstances(any(Region.class), any(String[].class));
  }

  @Test
  public void terminateInstances() {
    log.info("Testing terminate");
    List<ScheduledClass> classList = getClassStartsIn(-33, 1);
    when(cDao.getAll(ScheduledClass.class)).thenReturn(classList);
    cm.controlInstances();

    verify(aws).terminateInstances(any(Region.class), any(String[].class));
  }

  private List<ScheduledClass> getClassStartsIn(int hours, int numInstances) {
    ScheduledClass clazz= new ScheduledClass();
    Calendar start = new GregorianCalendar();
    start.add(Calendar.HOUR_OF_DAY, hours);
    clazz.setStartDate(new Date(start.getTimeInMillis()));
    clazz.setStartTime(new Time(start.getTimeInMillis()));
    start.add(Calendar.DATE, 1);
    start.add(Calendar.HOUR_OF_DAY, 8);
    clazz.setEndDate(new Date(start.getTimeInMillis()));
    clazz.setEndTime(new Time(start.getTimeInMillis()));
    clazz.setTimeZone(start.getTimeZone().getID());
    clazz.setId(1L);
    List<Instance> instances = new ArrayList<>();
    clazz.setInstances(instances);
    for (int i = 0; i < numInstances; i++) {
      Instance in = new Instance();
      in.setTerminated(false);
      in.setRegion(Region.AP_NORTHEAST_1.name());
      instances.add(in);
    }

    ClassTypeDetail detail = new ClassTypeDetail();
    detail.setRegion(Region.AP_NORTHEAST_1.name());
    clazz.setClassTypeDetail(detail);
    return new QuickList<>(clazz);
  }
}
