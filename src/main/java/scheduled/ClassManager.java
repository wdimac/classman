/**
 * Copyright (C) 2016 AppDynamics
 *
 * @author William Dimaculangan
 */
package scheduled;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.appdynamics.aws.AwsAdaptor;
import com.appdynamics.aws.AwsAdaptor.Region;
import com.google.inject.Inject;
import com.google.inject.Provider;
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
  @Inject
  Provider<EntityManager> entityManagerProvider;

  @Schedule(delay=PERIOD, initialDelay=2, timeUnit=TimeUnit.MINUTES)
  public void controlInstances() {
    classDao.clearSession();
    List<ScheduledClass> clazzes = classDao.getAll(ScheduledClass.class);
    for (ScheduledClass clazz: clazzes) {
      log.info("Class:" + clazz.getId());
      Calendar now = new GregorianCalendar();
      log.info("now:" + now);

      //start and end times for servers are on hour before/after classes
      Calendar startTime = new GregorianCalendar(TimeZone.getTimeZone(clazz.getTimeZone()));
      Date sDate = Date.valueOf(clazz.getStartDate());
      log.info("sDate:" + sDate);
      log.info("sTime" + clazz.getStartTime());
      startTime.set(sDate.getYear() +1900, sDate.getMonth(), sDate.getDate(),
                    clazz.getStartTime().getHours(), clazz.getStartTime().getMinutes(), 0);
      startTime.add(Calendar.HOUR_OF_DAY, -1);

      Calendar endTime = new GregorianCalendar(TimeZone.getTimeZone(clazz.getTimeZone()));
      Date eDate = Date.valueOf(clazz.getEndDate());
      endTime.set(eDate.getYear() +1900, eDate.getMonth(), eDate.getDate(),
                    clazz.getEndTime().getHours(), clazz.getEndTime().getMinutes(), 0);
      endTime.add(Calendar.HOUR_OF_DAY, +1);

      Calendar firstEndTime = new GregorianCalendar(TimeZone.getTimeZone(clazz.getTimeZone()));
      firstEndTime.set(sDate.getYear() +1900, sDate.getMonth(), sDate.getDate(),
                    clazz.getEndTime().getHours(), clazz.getEndTime().getMinutes(), 0);
      firstEndTime.add(Calendar.HOUR_OF_DAY, +1);

      log.info("endTime:" + endTime);
      if (now.before(startTime)) {
        log.info("Class pending.");
        continue;
      } else if (now.compareTo(endTime) >=0) {
        log.info("check stop:" + (now.getTimeInMillis() - endTime.getTimeInMillis()));
        if (now.getTimeInMillis() - endTime.getTimeInMillis() < HOUR) {
          log.info("Class over - terminating instances");
          checkAllTerminated(clazz);
        }
      } else {
        log.info("Checking to see if in start/stop period");
        Calendar relStart = (Calendar) now.clone();
        relStart.setTime(startTime.getTime());
        Calendar relEnd = (Calendar)now.clone();
        relEnd.setTime(firstEndTime.getTime());

        log.info("startTime:" + startTime);
        log.info("relStart:" + relStart);
        checkLifeCycle(clazz, now, relStart, relEnd);
      }
    }
  }

  private void checkLifeCycle(ScheduledClass clazz, Calendar now, Calendar startTime, Calendar endTime) {
    EntityManager em = entityManagerProvider.get();
    EntityTransaction trans = em.getTransaction();
    trans.begin();
    try {
      if (needToStart(now, startTime)) {
        List<Instance> ins;
        if (clazz.getInstances().isEmpty()) {
          ins = scController.startClassInstances(0, clazz);
          log.info("Created: " + ins.size() + " instances.");
        } else {
          List<String> ids = new ArrayList<>();
          for (Instance in : clazz.getInstances()) {
            ids.add(in.getId());
          }
          List<String> iIds = aws.startInstances(Region.valueOf(clazz.getClassTypeDetail().getRegion()),
              ids.toArray(new String[ids.size()]));
          log.info(iIds.size() + " instances started");
        }
      } else if (needToEnd(now, endTime)) {
        List<String> ids = new ArrayList<>();
        for (Instance in : clazz.getInstances()) {
          ids.add(in.getId());
        }
        List<String> iIds = aws.stopInstances(Region.valueOf(clazz.getClassTypeDetail().getRegion()),
            ids.toArray(new String[ids.size()]));
        log.info(iIds.size() + " instances stopped");
      }
    } finally {
      if (trans.getRollbackOnly()) {
        trans.rollback();
      } else {
        trans.commit();
      }
    }

  }

  private boolean needToEnd(Calendar now, Calendar endTime) {
    long diff = now.getTimeInMillis() - endTime.getTimeInMillis();
    diff = diff % (24 * HOUR);
    return (diff >= 0 && diff < HOUR);
  }

  private boolean needToStart(Calendar now, Calendar startTime) {
    long diff = now.getTimeInMillis() - startTime.getTimeInMillis();
    diff = diff % (24 * HOUR);
    return (diff >= 0 && diff < HOUR);
  }

  @Transactional
  private void checkAllTerminated(ScheduledClass clazz) {
    EntityManager em = entityManagerProvider.get();
    EntityTransaction trans = em.getTransaction();
    trans.begin();
    try {
      for (Instance inst : clazz.getInstances()) {
        List<String> ids = new ArrayList<>();
        if (!inst.isTerminated()) {
          ids.add(inst.getId());
          inst.setTerminated(true);
          instDao.update(inst);
        }
        if (!ids.isEmpty())
          aws.terminateInstances(Region.valueOf(inst.getRegion()), ids.toArray(new String[ids.size()]));
      }
    } finally {
      if (trans.getRollbackOnly()) {
        trans.rollback();
      } else {
        trans.commit();
      }
    }
  }
}
