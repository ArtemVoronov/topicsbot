package com.topicsbot.model.services;

import com.topicsbot.model.services.config.ConfigParamsHolder;
import com.topicsbot.model.services.config.ConfigParamsHolderBasic;
import com.topicsbot.model.services.db.DBService;
import com.topicsbot.model.services.db.HibernateDBService;
import com.topicsbot.model.services.db.KeyValueStorageService;
import com.topicsbot.model.services.db.RedisClient;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.ConfigurationConverter;
import org.apache.log4j.Logger;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Author: Artem Voronov
 */
public class ModelServicesFactory {

  public static DBService initDBService(Configuration hibernateProperties) throws ModelServicesException {
    try {
      return new HibernateDBService(ConfigurationConverter.getProperties(hibernateProperties));
    } catch(Exception e) {
      throw new ModelServicesException("Error during DBService initialization.", e);
    }
  }

  public static KeyValueStorageService initKeyValueStorageService(Configuration jedisConfigProperties) throws ModelServicesException {
    try {
      return new RedisClient(jedisConfigProperties);
    } catch(Exception e) {
      throw new ModelServicesException("Error during KeyValueStorageService initialization.", e);
    }
  }

  public static ConfigParamsHolder initConfigParamsHolder(DBService db, Logger logger, long rereadConfigParamsDelayInSeconds) throws ModelServicesException {
    try {
      return new ConfigParamsHolderBasic(db, logger, 200L, rereadConfigParamsDelayInSeconds);
    }
    catch(Exception e) {
      throw new ModelServicesException("Error during ConfigParamsHolder initialization.", e);
    }
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
