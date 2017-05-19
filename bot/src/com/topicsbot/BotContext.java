package com.topicsbot;

import com.topicsbot.services.Services;
import com.topicsbot.services.db.DBService;
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


  private BotContext(Configuration config) throws Exception {
    this.services = new Services(config);
  }

  static void init(Configuration config) throws Exception {
    if(instance == null) {
      instance = new BotContext(config);
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

  @Produces
  public DBService getDBService() {
    return services.getDbService();
  }
}