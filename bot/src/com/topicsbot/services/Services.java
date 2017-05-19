package com.topicsbot.services;

import com.topicsbot.services.api.telegram.TelegramApiService;
import com.topicsbot.services.api.telegram.TelegramApiProvider;
import com.topicsbot.services.db.DBService;
import org.apache.commons.configuration2.Configuration;

import java.util.Properties;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class Services {

  private final DBService dbService;
  private final ScheduledExecutorService scheduledExecutorService;
  private final TelegramApiProvider telegramApiProvider;

  public Services(Configuration config) throws ServicesException {
    this.dbService = initDBService(config);
    this.scheduledExecutorService = initScheduledExecutorService(config);
    this.telegramApiProvider = initTelegramApiProvider(config, scheduledExecutorService);
  }

  private DBService initDBService(Configuration config) throws ServicesException {
    try {
      Properties hibernateProperties = new Properties();
      hibernateProperties.put("hibernate.connection.url", config.getString("hibernate.connection.url"));
      hibernateProperties.put("hibernate.connection.username", config.getString("hibernate.connection.username"));
      hibernateProperties.put("hibernate.connection.password", config.getString("hibernate.connection.password"));
      hibernateProperties.put("hibernate.dialect", config.getString("hibernate.dialect"));
      hibernateProperties.put("hibernate.connection.driver_class", config.getString("hibernate.connection.driver_class"));
      hibernateProperties.put("hibernate.hbm2ddl.auto", config.getString("hibernate.hbm2ddl.auto"));
      hibernateProperties.put("hibernate.connection.provider_class", config.getString("hibernate.connection.provider_class"));
      hibernateProperties.put("hibernate.c3p0.timeout", config.getString("hibernate.c3p0.timeout"));
      hibernateProperties.put("hibernate.c3p0.max_statements", config.getString("hibernate.c3p0.max_statements"));
      hibernateProperties.put("hibernate.c3p0.idle_test_period", config.getString("hibernate.c3p0.idle_test_period"));
      hibernateProperties.put("hibernate.c3p0.preferredTestQuery", config.getString("hibernate.c3p0.preferredTestQuery"));
      hibernateProperties.put("hibernate.c3p0.testConnectionOnCheckout", config.getString("hibernate.c3p0.testConnectionOnCheckout"));
      hibernateProperties.put("application.id", config.getString("application.id"));
      return new DBService(hibernateProperties);
    } catch(Exception e) {
      throw new ServicesException("Error during DBService initialization.", e);
    }
  }

  private ScheduledExecutorService initScheduledExecutorService(Configuration config) throws ServicesException {
    try {
      int corePoolSize = config.getInt("thread.pool.core.pool.size");
      int maximumPoolSize = config.getInt("thread.pool.maximum.pool.size");
      long keepAliveTime = config.getInt("thread.pool.keep.alive.time.sec");

      ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(corePoolSize, new ThreadFactoryWithCounter("ServicesWorker-", 0));
      executor.setMaximumPoolSize(maximumPoolSize);
      executor.setKeepAliveTime(keepAliveTime, TimeUnit.SECONDS);

      return executor;
    } catch (Exception e) {
      throw new ServicesException("Error during ScheduledExecutorService initialization", e);
    }
  }

  private TelegramApiProvider initTelegramApiProvider(Configuration config, ScheduledExecutorService scheduledExecutorService) throws ServicesException {
    try {
      boolean testMode = config.getBoolean("test.mode", false);
      int connectTimeout = config.getInt("telegram.api.client.connect.timeout.millis");
      int requestTimeout = config.getInt("telegram.api.client.request.timeout.millis");
      String botToken = config.getString(testMode ? "test.bot.token" : "bot.token");
      return new TelegramApiService(scheduledExecutorService, connectTimeout, requestTimeout, botToken);
    } catch (Exception e) {
      throw new ServicesException("Error during TelegramApiProvider initialization", e);
    }
  }

  public DBService getDbService() {
    return dbService;
  }

  public void shutdown() {
    dbService.shutdown();
    scheduledExecutorService.shutdown();
  }

  private static class ThreadFactoryWithCounter implements ThreadFactory {
    private int counter;
    private String threadNamePrefix;

    ThreadFactoryWithCounter(String threadNamePrefix, int initialCounter) {
      this.threadNamePrefix = threadNamePrefix;
      this.counter = initialCounter;
    }

    Thread createThread(Runnable r, String threadName) {
      return new Thread(r, threadName);
    }

    public final Thread newThread(Runnable r) {
      return this.createThread(r, this.threadNamePrefix + this.counter++);
    }
  }

}
