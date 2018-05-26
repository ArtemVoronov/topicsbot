package com.topicsbot.ui.telegram.services.communication;

import com.topicsbot.model.services.config.ConfigParamsHolder;
import com.topicsbot.ui.telegram.services.communication.model.Updates;

/**
 * Author: Artem Voronov
 */
public class TelegramBotApiProviderBasic implements TelegramBotApiProvider {


  private final ConfigParamsHolder configParamsHolder;

  public TelegramBotApiProviderBasic(ConfigParamsHolder configParamsHolder) {
    this.configParamsHolder = configParamsHolder;
//    final String apiTelegramUrl = "https://api.telegram.org/bot"+configParamsHolder.getConfigParam("telegram.bot.token"); //TODO: use editable config
//    this.getUpdatesUrl = getUpdatesUrl();
  }

  private String getApiTelegramUrl() {
    return "https://api.telegram.org/bot"+configParamsHolder.getConfigParam("telegram.bot.token");
  }

  private String getUpdatesUrl() {
    return getApiTelegramUrl() + "/getUpdates";
  }

  @Override
  public Updates getUpdates() {
    return null;
  }

  @Override
  public Updates getUpdates(int offset) {
    return null;
  }
}
