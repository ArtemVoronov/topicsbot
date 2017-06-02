package com.topicsbot.services;

import com.topicsbot.services.analysis.AnalysisService;
import com.topicsbot.services.api.telegram.TelegramApiService;
import com.topicsbot.services.api.telegram.TelegramApiProvider;
import com.topicsbot.services.cache.CacheService;
import com.topicsbot.services.db.DBService;
import com.topicsbot.services.i18n.ResourceBundleService;
import com.topicsbot.utils.ThreadFactoryWithCounter;
import org.apache.commons.configuration2.Configuration;

import java.util.Properties;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Services {

  private final DBService dbService;
  private final ScheduledExecutorService scheduledExecutorService;
  private final TelegramApiProvider telegramApiProvider;
  private final ResourceBundleService resourceBundleService;
  private final AnalysisService analysisService;
  private final CacheService cacheService;

  public Services(Configuration config) throws ServicesException {
    this.resourceBundleService = new ResourceBundleService();
    this.dbService = initDBService(config);
    this.scheduledExecutorService = initScheduledExecutorService(config);
    this.analysisService = initAnalysisService(config);
    this.cacheService = initCacheService(config, dbService, scheduledExecutorService);
    this.telegramApiProvider = initTelegramApiProvider(config, dbService, scheduledExecutorService, resourceBundleService, analysisService, cacheService);

  }

  private DBService initDBService(Configuration config) throws ServicesException {
    try {
      Properties hibernateProperties = new Properties();
      hibernateProperties.put("hibernate.connection.url", config.getString("db.hibernate.connection.url"));
      hibernateProperties.put("hibernate.connection.username", config.getString("db.hibernate.connection.username"));
      hibernateProperties.put("hibernate.connection.password", config.getString("db.hibernate.connection.password"));
      hibernateProperties.put("hibernate.dialect", config.getString("db.hibernate.dialect"));
      hibernateProperties.put("hibernate.connection.driver_class", config.getString("db.hibernate.connection.driver_class"));
      hibernateProperties.put("hibernate.hbm2ddl.auto", config.getString("db.hibernate.hbm2ddl.auto"));
      hibernateProperties.put("hibernate.connection.provider_class", config.getString("db.hibernate.connection.provider_class"));
      hibernateProperties.put("hibernate.c3p0.timeout", config.getString("db.hibernate.c3p0.timeout"));
      hibernateProperties.put("hibernate.c3p0.max_statements", config.getString("db.hibernate.c3p0.max_statements"));
      hibernateProperties.put("hibernate.c3p0.idle_test_period", config.getString("db.hibernate.c3p0.idle_test_period"));
      hibernateProperties.put("hibernate.c3p0.preferredTestQuery", config.getString("db.hibernate.c3p0.preferredTestQuery"));
      hibernateProperties.put("hibernate.c3p0.testConnectionOnCheckout", config.getString("db.hibernate.c3p0.testConnectionOnCheckout"));
      hibernateProperties.put("application.id", config.getString("db.application.id"));

      String hibernateAddCfg = "/" + DBService.class.getPackage().getName().replace('.', '/') + "/hibernate.cfg.xml";

      return new DBService(hibernateProperties, hibernateAddCfg);
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

  private TelegramApiProvider initTelegramApiProvider(Configuration config, DBService db,
                                                      ScheduledExecutorService scheduledExecutorService,
                                                      ResourceBundleService resourceBundleService,
                                                      AnalysisService analysisService, CacheService cacheService) throws ServicesException {
    try {
      boolean testMode = config.getBoolean("test.mode", false);
      int connectTimeout = config.getInt("telegram.api.client.connect.timeout.millis");
      int requestTimeout = config.getInt("telegram.api.client.request.timeout.millis");
      String botToken = config.getString(testMode ? "test.bot.token" : "bot.token");
      String botUserName = config.getString(testMode ? "test.bot.username" : "bot.username");
      return new TelegramApiService(db, scheduledExecutorService, resourceBundleService, analysisService, cacheService, connectTimeout, requestTimeout, botToken, botUserName);
    } catch (Exception e) {
      throw new ServicesException("Error during TelegramApiProvider initialization", e);
    }
  }

  private AnalysisService initAnalysisService(Configuration config) throws ServicesException {
    try {
      String pathToStopWordsDir = config.getString("path.to.stop.words");
      String pathToLuceneIndexesDir = config.getString("path.to.lucene.indexes");
      String pathToWorldLuceneIndexesDir = config.getString("path.to.world.lucene.indexes");
      return new AnalysisService(pathToStopWordsDir, pathToLuceneIndexesDir, pathToWorldLuceneIndexesDir);
    } catch (Exception e) {
      throw new ServicesException("Error during AnalysisService initialization", e);
    }
  }

  private CacheService initCacheService(Configuration config, DBService db, ScheduledExecutorService scheduledExecutorService) throws ServicesException {
    try {
      String pathToCacheDir = config.getString("path.cache");
      if (pathToCacheDir == null)
        throw new IllegalArgumentException("missed path cache param");
      return new CacheService(pathToCacheDir, db, scheduledExecutorService);
    } catch (Exception e) {
      throw new ServicesException("Error during CacheService initialization", e);
    }
  }

  public DBService getDbService() {
    return dbService;
  }

  public TelegramApiProvider getTelegramApiProvider() {
    return telegramApiProvider;
  }

  public ResourceBundleService getResourceBundleService() {
    return resourceBundleService;
  }

  public AnalysisService getAnalysisService() {
    return analysisService;
  }

  public CacheService getCacheService() {
    return cacheService;
  }

  public void shutdown() {
    dbService.shutdown();
    scheduledExecutorService.shutdown();
    cacheService.shutdown();
  }

}
