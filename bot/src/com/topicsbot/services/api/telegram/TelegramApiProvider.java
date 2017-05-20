package com.topicsbot.services.api.telegram;

import com.topicsbot.services.api.telegram.model.Chat;

/**
 * Author: Artem Voronov
 */
public interface TelegramApiProvider {
  void sendMessage(Chat chat, String text);
  int getChatMembersCount(Chat chat);
}
