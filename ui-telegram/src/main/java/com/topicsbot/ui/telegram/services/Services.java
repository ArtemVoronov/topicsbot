package com.topicsbot.ui.telegram.services;

import com.topicsbot.ui.telegram.services.communication.TelegramBotApiProvider;
import org.apache.commons.configuration2.Configuration;

/**
 * Author: Artem Voronov
 */
public class Services {

  private final TelegramBotApiProvider telegramBotApiProvider;

  public Services(Configuration config) {
    this.telegramBotApiProvider = initTelegramBotApiProvider();
  }

  public void shutdown() {
    //TODO
  }

  private static TelegramBotApiProvider initTelegramBotApiProvider() {
    try {
      //TODO:
      throw new UnsupportedOperationException("TODO");
    }
    catch (Exception e) {
      throw new RuntimeException("Error during TelegramBotApiProvider initialization.", e);
    }
  }
}
