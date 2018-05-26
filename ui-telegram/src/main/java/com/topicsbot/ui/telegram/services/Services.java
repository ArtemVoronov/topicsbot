package com.topicsbot.ui.telegram.services;

import com.topicsbot.model.entities.config.ConfigParam;
import com.topicsbot.model.query.ConfigParamsQuery;
import com.topicsbot.model.services.ModelServicesException;
import com.topicsbot.model.services.ModelServicesFactory;
import com.topicsbot.model.services.config.ConfigParamsHolder;
import com.topicsbot.model.services.db.DBService;
import com.topicsbot.model.services.db.KeyValueStorageService;
import com.topicsbot.ui.telegram.services.communication.TelegramBotApiProvider;
import com.topicsbot.ui.telegram.services.communication.TelegramBotApiProviderBasic;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.log4j.Logger;

import java.net.URL;
import java.util.List;


/**
 * Author: Artem Voronov
 */
public class Services {

  private final DBService db;
  private final KeyValueStorageService keyValueStorage;
  private final ConfigParamsHolder configParamsHolder;
  private final TelegramBotApiProvider telegramBotApiProvider;

  public Services() throws ConfigurationException, ModelServicesException, UiTelegramServicesException {
    ClassLoader classLoader = Services.class.getClassLoader();
    URL dbConfig = classLoader.getResource("db_service_config.properties");
    URL kvConfig = classLoader.getResource("kv_storage_config.properties");
    final Configurations configs = new Configurations();

    this.db = ModelServicesFactory.initDBService(configs.properties(dbConfig));
    this.keyValueStorage = ModelServicesFactory.initKeyValueStorageService(configs.properties(kvConfig));
    this.configParamsHolder = ModelServicesFactory.initConfigParamsHolder(db, Logger.getLogger("UI_TELEGRAM_CONFIG_PARAMS_HOLDER"), 300);
    this.telegramBotApiProvider = initTelegramBotApiProvider(configParamsHolder);

    //TODO: add JMX service init
  }

  public void shutdown() {
    db.destroy();
    keyValueStorage.destroy();
    configParamsHolder.shutdown();

    //TODO: add JMX service destroy
  }

  private static TelegramBotApiProvider initTelegramBotApiProvider(ConfigParamsHolder configParamsHolder) throws UiTelegramServicesException {
    try {
      return new TelegramBotApiProviderBasic(configParamsHolder);
    }
    catch (Exception e) {
      throw new UiTelegramServicesException("Error during TelegramBotApiProvider initialization.", e);
    }
  }


  //TODO: clean
  //TODO: add integration tests
  @SuppressWarnings("unchecked")
  public static void main(String[] args) throws ConfigurationException, ModelServicesException {

    ClassLoader classLoader = Services.class.getClassLoader();
    URL dbConfig = classLoader.getResource("db_service_config.properties");
    URL kvConfig = classLoader.getResource("kv_storage_config.properties");
    final Configurations configs = new Configurations();
    DBService db = ModelServicesFactory.initDBService(configs.properties(dbConfig));
    KeyValueStorageService keyValueStorage = ModelServicesFactory.initKeyValueStorageService(configs.properties(kvConfig));

    db.vtx(s -> {
      List<ConfigParam> configParams = ConfigParamsQuery.all(s).list();
      configParams.forEach(c-> System.out.println(c.getParamName() + " = " + c.getParamValue()));
    });


    System.out.println("--------------------------------");
    System.out.println(keyValueStorage.get("test"));
  }
}
