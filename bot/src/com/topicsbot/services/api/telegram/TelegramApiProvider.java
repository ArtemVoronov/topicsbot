package com.topicsbot.services.api.telegram;

import com.topicsbot.services.api.telegram.model.Chat;
import com.topicsbot.services.api.telegram.model.Message;

/**
 * Author: Artem Voronov
 */
public interface TelegramApiProvider {
  void sendMessage(Chat chat, String text);
  void replyToMessage(Chat chat, String text, Message message);
  int getChatMembersCount(Chat chat);
}
