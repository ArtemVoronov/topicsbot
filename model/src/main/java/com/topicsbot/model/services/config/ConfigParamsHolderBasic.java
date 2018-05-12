package com.topicsbot.model.services.config;

import com.topicsbot.model.entities.config.ConfigParam;
import com.topicsbot.model.query.ConfigParamsQuery;
import com.topicsbot.model.services.ModelServicesException;
import com.topicsbot.model.services.ModelServicesFactory;
import com.topicsbot.model.services.db.DBService;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

/**
 * author: Artem Voronov
 */
public class ConfigParamsHolderBasic implements ConfigParamsHolder {

  private final ScheduledExecutorService executor;
  private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
  private final ReentrantReadWriteLock.ReadLock readlock = lock.readLock();
  private final ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();

  private final DBService db;
  private final ConfigParamsReader configParamsReader;
  private final Logger logger;
  private final Map<String, String> configParams;

  public ConfigParamsHolderBasic(DBService db, Logger logger, long initialDelay, long rereadServiceParamsDelayInSeconds) throws ModelServicesException {
    this(db, logger);
    executor.scheduleWithFixedDelay(configParamsReader, initialDelay, rereadServiceParamsDelayInSeconds, TimeUnit.SECONDS);
  }

  ConfigParamsHolderBasic(DBService db, Logger logger) throws ModelServicesException {
    this.db = db;
    this.logger = logger;
    this.executor = ModelServicesFactory.initScheduledExecutorService("CONFIG_PARAMS_HOLDER-");
    this.configParamsReader = new ConfigParamsReader();

    configParams = readConfigParams();

    if (logger.isInfoEnabled())
      logger.info("config params initiated:\n" + prettyFormat(configParams));
  }

  @Override
  public void shutdown() {
    executor.shutdown();
  }

  @Override
  public String getConfigParam(String paramName) {
    try {
      readlock.lock();
      return configParams.get(paramName);
    } finally {
      readlock.unlock();
    }
  }


  @SuppressWarnings("unchecked")
  private Map<String, String> readConfigParams() {
    return db.tx( s -> {
      List<ConfigParam> configParams = ConfigParamsQuery.all(s).list();
      return configParams.stream().collect(Collectors.toMap(ConfigParam::getParamName, ConfigParam::getParamValue));
    });
  }

  private void updateConfigParams() {
    try {
      writeLock.lock();

      Map<String, String> updatedConfigParams = readConfigParams();

      boolean theSame = configParams.equals(updatedConfigParams);

      if (theSame)
        return;

      configParams.clear();
      configParams.putAll(updatedConfigParams);

      if (logger.isInfoEnabled())
        logger.info("config params updated:\n" + prettyFormat(configParams));

    } catch (Exception ex) {
      if (logger.isInfoEnabled())
        logger.info("config params updating failed, current state is:\n" + prettyFormat(configParams));
    } finally {
      writeLock.unlock();
    }
  }

  private static String prettyFormat(Map<String, String> params) {
    return params.entrySet().stream().map(e -> e.getKey() + "=" + e.getValue()).collect(Collectors.joining("\n"));
  }

  private class ConfigParamsReader implements Runnable {

    @Override
    public void run() {
      updateConfigParams();
    }

  }

  ConfigParamsReader getConfigParamsReader() {
    return configParamsReader;
  }
}
