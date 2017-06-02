package com.topicsbot.services.api.telegram;

import com.topicsbot.services.api.telegram.model.*;

/**
 * Author: Artem Voronov
 */
public interface TelegramApiProvider {
  Updates getUpdates(Integer lastUpdateId);
  void sendMessage(Chat chat, String text);
  void replyToMessage(Chat chat, String text, Message message);
  void sendReplyKeyboard(Chat chat, String text, ReplyKeyboardMarkup keyboardMarkup);
  void hideKeyboard(Chat chat, String text, ReplyKeyboardRemove replyKeyboardRemove);
  void sendInlineKeyboard();
  int getChatMembersCount(Chat chat);
}
