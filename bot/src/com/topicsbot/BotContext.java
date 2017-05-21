package com.topicsbot;

import com.topicsbot.services.Services;
import com.topicsbot.services.api.telegram.TelegramApiProvider;
import com.topicsbot.services.db.DBService;
import com.topicsbot.services.i18n.ResourceBundleService;
import org.apache.commons.configuration2.Configuration;

import javax.ws.rs.Produces;
import java.util.concurrent.CountDownLatch;

/**
 * author: Artem Voronov
 */
public class BotContext {
  private final static CountDownLatch initLatch = new CountDownLatch(1);
  private static BotContext instance;

  private final Services services;
  private final String version;


  private BotContext(Configuration config, String version) throws Exception {
    this.services = new Services(config);
    this.version = version;
  }

  static void init(Configuration config, String version) throws Exception {
    if(instance == null) {
      instance = new BotContext(config, version);
      initLatch.countDown();
    }
  }

  public static BotContext getInstance() {
    try {
      initLatch.await();
      return instance;
    }
    catch(InterruptedException e) {
      throw new RuntimeException("BotContext instance is not initialized", e);
    }
  }

  void shutdown() {
    services.shutdown();
  }

  public Services getServices() {
    return services;
  }

  public String getVersion() {
    return version;
  }

  @Produces
  public DBService getDBService() {
    return services.getDbService();
  }

  @Produces
  public TelegramApiProvider getTelegramApiProvider() {
    return services.getTelegramApiProvider();
  }

  @Produces
  public ResourceBundleService getResourceBundleService() {
    return services.getResourceBundleService();
  }
}