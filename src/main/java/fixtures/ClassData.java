package fixtures;

import java.sql.Date;
import java.sql.Time;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.TimeZone;

import javax.persistence.EntityManager;

import com.amazonaws.services.ec2.model.InstanceType;
import com.appdynamics.aws.AwsAdaptor.Region;

import models.ClassType;
import models.ClassTypeDetail;
import models.Images;
import models.Instance;
import models.ScheduledClass;
import models.SecurityGroup;
import models.User;

public class ClassData implements Fixture {
  private static DateFormat formatter= new SimpleDateFormat("yyyy-MM-dd");

  @Override
  public void run(EntityManager entityManager, HashMap<String, String> results) {
    ClassType type = new ClassType();
    entityManager.persist(type);

    Images image = new Images();
    image.setId("ID-1");
    image.setRegion(Region.US_EAST_1.name());
    entityManager.persist(image);

    SecurityGroup group = new SecurityGroup();
    group.setId("sg-1");
    group.setOwnerId("xxx");
    group.setRegion(Region.US_EAST_1.name());
    entityManager.persist(group);

    ClassTypeDetail detail = new ClassTypeDetail();
    detail.setRegion(Region.US_EAST_1.name());
    detail.setClassType(type);
    detail.setInstanceType(InstanceType.M3Xlarge.name());
    detail.setImageId("ID-1");
    detail.setSecurityGroupId("sg-1");
    entityManager.persist(detail);

    User instructor = new User();
    instructor.setFirstName("test");
    instructor.setLastName("Last");
    entityManager.persist(instructor);

    ScheduledClass clazz = new ScheduledClass();
    clazz.setDescription("test");
    clazz.setClassTypeDetail(detail);
    Calendar cal = new GregorianCalendar();
    cal.add(Calendar.MONTH, 1);
    clazz.setStartDate(formatter.format(new Date(cal.getTimeInMillis())));
    cal.add(Calendar.DAY_OF_MONTH, 2);
    clazz.setEndDate(formatter.format(new Date(cal.getTimeInMillis())));
    clazz.setStartTime(Time.valueOf("09:00:00"));
    clazz.setEndTime(Time.valueOf("16:00:00"));
    clazz.setTimeZone(TimeZone.getDefault().getID());
    clazz.setInstructor(instructor);
    entityManager.persist(clazz);

    Instance inst = new Instance();
    inst.setId("i-INST1");
    inst.setRegion(Region.US_EAST_1.name());
    inst.setScheduledClass(clazz);
    inst.setImage_id(image.getId());
    entityManager.persist(inst);

    clazz = new ScheduledClass();
    clazz.setDescription("test2");
    clazz.setClassTypeDetail(detail);
    cal = new GregorianCalendar();
    cal.add(Calendar.MONTH, 2);
    clazz.setStartDate(formatter.format(new Date(cal.getTimeInMillis())));
    cal.add(Calendar.DAY_OF_MONTH, 2);
    clazz.setEndDate(formatter.format(new Date(cal.getTimeInMillis())));
    clazz.setStartTime(Time.valueOf("09:00:00"));
    clazz.setEndTime(Time.valueOf("16:00:00"));
    clazz.setTimeZone(TimeZone.getDefault().getID());
    entityManager.persist(clazz);

    results.put("ClassData", "Inserted default records");
  }

}
