package com.topicsbot.ui.telegram.services.communication;

import com.topicsbot.ui.telegram.services.communication.model.Updates;

/**
 * Author: Artem Voronov
 */
public class TelegramBotApiProviderBasic implements TelegramBotApiProvider {


  private final String getUpdatesUrl;

  public TelegramBotApiProviderBasic(String botToken) {

    final String apiTelegramUrl = "https://api.telegram.org/bot"+botToken; //TODO: use editable config
    this.getUpdatesUrl = apiTelegramUrl + "/getUpdates";
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
