package fixtures;

import java.sql.Date;
import java.sql.Time;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.TimeZone;

import javax.persistence.EntityManager;

import com.amazonaws.regions.Regions;

import models.ClassType;
import models.ClassTypeDetail;
import models.ScheduledClass;

public class ClassData implements Fixture {

  @Override
  public void run(EntityManager entityManager, HashMap<String, String> results) {
    ClassType type = new ClassType();
    entityManager.persist(type);

    ClassTypeDetail detail = new ClassTypeDetail();
    detail.setRegion(Regions.US_EAST_1.getName());
    detail.setClassType(type);
    entityManager.persist(detail);

    ScheduledClass clazz = new ScheduledClass();
    clazz.setDescription("test");
    clazz.setClassTypeDetail(detail);
    Calendar cal = new GregorianCalendar();
    cal.add(Calendar.MONTH, 1);
    clazz.setStartDate(new Date(cal.getTimeInMillis()));
    cal.add(Calendar.DAY_OF_MONTH, 2);
    clazz.setEndDate(new Date(cal.getTimeInMillis()));
    clazz.setStartTime(Time.valueOf("09:00:00"));
    clazz.setEndTime(Time.valueOf("16:00:00"));
    clazz.setTimeZone(TimeZone.getDefault().getID());
    entityManager.persist(clazz);

    results.put("ClassData", "Inserted default records");
  }

}
