package com.topicsbot.ui.telegram.services;

import com.topicsbot.model.entities.config.ConfigParam;
import com.topicsbot.model.query.ConfigParamsQuery;
import com.topicsbot.model.services.ModelServicesException;
import com.topicsbot.model.services.ModelServicesFactory;
import com.topicsbot.model.services.db.DBService;
import com.topicsbot.model.services.db.KeyValueStorageService;
import com.topicsbot.ui.telegram.services.communication.TelegramBotApiProvider;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.ConfigurationConverter;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;

import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Properties;


/**
 * Author: Artem Voronov
 */
public class Services {

//  private final DBService db;
//  private final KeyValueStorageService keyValueStorage;
//  private final TelegramBotApiProvider telegramBotApiProvider;

  public Services(Configuration config) {
//    this.telegramBotApiProvider = initTelegramBotApiProvider();
  }

  public void shutdown() {
    //TODO
  }

//  private static DBService initDBService() {
//    try {
//      //TODO:
//      throw new UnsupportedOperationException("TODO");
//    }
//    catch (Exception e) {
//      throw new UITelegramServicesException("Error during DBService initialization.", e);
//    }
//  }
//
//  private static KeyValueStorageService initKeyValueStorageService() {
//    try {
//      //TODO:
//      throw new UnsupportedOperationException("TODO");
//    }
//    catch (Exception e) {
//      throw new UITelegramServicesException("Error during TelegramBotApiProvider initialization.", e);
//    }
//  }
//
//  private static TelegramBotApiProvider initTelegramBotApiProvider() {
//    try {
//      //TODO:
//      throw new UnsupportedOperationException("TODO");
//    }
//    catch (Exception e) {
//      throw new UITelegramServicesException("Error during TelegramBotApiProvider initialization.", e);
//    }
//  }


  public static void main(String[] args) throws URISyntaxException, ConfigurationException, ModelServicesException {
    ClassLoader classLoader = Services.class.getClassLoader();
    URL dbConfig = classLoader.getResource("db_service_config.properties");
    URL kvConfig = classLoader.getResource("kv_storage_config.properties");
    final Configurations configs = new Configurations();

    DBService db = ModelServicesFactory.initDBService(configs.properties(dbConfig)); //TODO: unable connecto to localhost
    KeyValueStorageService keyValueStorage = ModelServicesFactory.initKeyValueStorageService(configs.properties(kvConfig));

    db.vtx(s -> {
      List<ConfigParam> configParams = ConfigParamsQuery.all(s).list();
      configParams.stream().forEach(c-> System.out.println(c.getParamName() + " = " + c.getParamValue()));
    });
  }
}
