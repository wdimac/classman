/**
 * Copyright (C) 2016 AppDynamics
 *
 * @author William Dimaculangan
 */
package scheduled;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.appdynamics.aws.AwsAdaptor;
import com.appdynamics.aws.AwsAdaptor.Region;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;

import controllers.ScheduledClassesController;
import dao.SimpleDao;
import models.Instance;
import models.ScheduledClass;
import ninja.scheduler.Schedule;

@Singleton
public class ClassManager {
  static final Logger log = LoggerFactory.getLogger(ClassManager.class);
  static final long PERIOD = 15;
  static final long HOUR = 60 * 60 * 1000;

  @Inject
  SimpleDao<ScheduledClass> classDao;
  @Inject
  SimpleDao<Instance> instDao;
  @Inject
  AwsAdaptor aws;
  @Inject
  ScheduledClassesController scController;

  @Schedule(delay=PERIOD, timeUnit=TimeUnit.SECONDS)
  public void controlInstances() {
    List<ScheduledClass> clazzes = classDao.getAll(ScheduledClass.class);
    for (ScheduledClass clazz: clazzes) {
      log.info("Class:" + clazz.getId());
      Calendar now = new GregorianCalendar();

      //start and end times for servers are on hour before/after classes
      Calendar startTime = new GregorianCalendar(TimeZone.getTimeZone(clazz.getTimeZone()));
      startTime.setTime(clazz.getStartDate());
      startTime.set(Calendar.HOUR_OF_DAY, clazz.getStartTime().getHours());
      startTime.set(Calendar.MINUTE, clazz.getStartTime().getMinutes());
      startTime.add(Calendar.HOUR_OF_DAY, -1);

      Calendar endTime = new GregorianCalendar(TimeZone.getTimeZone(clazz.getTimeZone()));
      endTime.setTime(clazz.getEndDate());
      endTime.set(Calendar.HOUR_OF_DAY, clazz.getEndTime().getHours());
      endTime.set(Calendar.MINUTE, clazz.getEndTime().getMinutes());
      endTime.add(Calendar.HOUR_OF_DAY, +1);
      if (now.before(startTime)) {
        log.info("Class pending.");
        continue;
      } else if (now.after(endTime) || now.equals(endTime)) {
        if (endTime.getTimeInMillis() - now.getTimeInMillis() < HOUR) {
          log.info("Class over - terminating instances");
          checkAllTerminated(clazz);
        }
      } else {
        log.info("Checking to see if in start/stop period");
        Calendar relStart = (Calendar) now.clone();
        relStart.setTime(startTime.getTime());
        Calendar relEnd = (Calendar)now.clone();
        relEnd.setTime(endTime.getTime());
        checkLifeCycle(clazz, now, relStart, relEnd);
      }
    }
  }

  @Transactional
  private void checkLifeCycle(ScheduledClass clazz, Calendar now, Calendar startTime, Calendar endTime) {
    if (needToStart(now, startTime)) {
      List<Instance> ins;
      if (clazz.getInstances().isEmpty()) {
        ins = scController.startClassInstances(0, clazz);
        log.info("Created: " + ins.size() + " instances.");
      } else {
        List<String> ids = new ArrayList<>();
        for (Instance in: clazz.getInstances()) {
          ids.add(in.getId());
        }
        List<String> iIds = aws.startInstances(Region.valueOf(clazz.getClassTypeDetail().getRegion()), ids.toArray(new String[ids.size()]));
        log.info(iIds.size() + " instances started");
      }
    } else if (needToEnd(now, endTime)) {
      List<String> ids = new ArrayList<>();
      for (Instance in: clazz.getInstances()) {
        ids.add(in.getId());
      }
      List<String> iIds = aws.stopInstances(Region.valueOf(clazz.getClassTypeDetail().getRegion()), ids.toArray(new String[ids.size()]));
      log.info(iIds.size() + " instances stopped");
    }
  }

  private boolean needToEnd(Calendar now, Calendar endTime) {
    int diff = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
      - (endTime.get(Calendar.HOUR_OF_DAY) * 60 + endTime.get(Calendar.MINUTE));
    log.info("end diff:" + diff);
    return (diff >= 0 && diff < 60);
  }

  private boolean needToStart(Calendar now, Calendar startTime) {
    int diff = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
        - (startTime.get(Calendar.HOUR_OF_DAY) * 60 + startTime.get(Calendar.MINUTE));
    return (diff >= 0 && diff < 60);
  }

  @Transactional
  private void checkAllTerminated(ScheduledClass clazz) {
    for (Instance inst: clazz.getInstances()) {
      List<String> ids = new ArrayList<>();
      if (!inst.isTerminated()) {
        ids.add(inst.getId());
        inst.setTerminated(true);
        instDao.update(inst);
      }
      if (!ids.isEmpty())
        aws.terminateInstances(Region.valueOf(inst.getRegion()),
          ids.toArray(new String[ids.size()]));
    }
  }
}
