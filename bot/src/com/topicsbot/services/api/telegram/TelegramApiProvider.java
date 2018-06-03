package com.topicsbot.services.api.telegram;

import com.topicsbot.services.api.telegram.model.*;


/**
 * Author: Artem Voronov
 */
public interface TelegramApiProvider {
  void sendMessage(String chatExternalId, String text);
  Chat getChat(String externalId);
}
