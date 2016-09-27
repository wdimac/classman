/**
 * Copyright (C) 2016 AppDynamics
 *
 * @author William Dimaculangan
 */

package conf;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.jensfendler.ninjaquartz.NinjaQuartzModule;

import scheduled.ClassManager;

@Singleton
public class Module extends AbstractModule {

  protected void configure() {
    bind(ClassManager.class);
    install( new NinjaQuartzModule() );
  }

}
