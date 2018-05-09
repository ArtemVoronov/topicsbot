package com.topicsbot.model.services;

import com.topicsbot.model.services.config.ConfigParamsHolder;
import com.topicsbot.model.services.db.DBService;
import com.topicsbot.model.services.db.KeyValueStorageService;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Author: Artem Voronov
 */
public class ModelServicesFactory {

  public static DBService initDBService() {
    throw new UnsupportedOperationException("TODO");
  }

  public static KeyValueStorageService initKeyValueStorageService() {
    throw new UnsupportedOperationException("TODO");
  }

  public static ConfigParamsHolder initConfigParamsHolder() {
    throw new UnsupportedOperationException("TODO");
  }

  public static ScheduledExecutorService initScheduledExecutorService(int corePoolSize, int maximumPoolSize, long keepAliveTime, String threadFactoryPrefix) throws ModelServicesException {
    try {
      ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(corePoolSize, new ThreadFactoryWithCounter(threadFactoryPrefix, 0));
      executor.setMaximumPoolSize(maximumPoolSize);
      executor.setKeepAliveTime(keepAliveTime, TimeUnit.SECONDS);

      return executor;
    } catch (Exception e) {
      throw new ModelServicesException("Error during ScheduledExecutorService initialization", e);
    }
  }

  public static ScheduledExecutorService initScheduledExecutorService(String threadFactoryPrefix) throws ModelServicesException {
    try {
      return Executors.newSingleThreadScheduledExecutor(new ThreadFactoryWithCounter(threadFactoryPrefix, 0));
    } catch (Exception e) {
      throw new ModelServicesException("Error during ScheduledExecutorService initialization", e);
    }
  }
}
