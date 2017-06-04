package com.topicsbot.services.api.telegram;

import com.topicsbot.services.api.telegram.model.*;

import java.util.List;

/**
 * Author: Artem Voronov
 */
public interface TelegramApiProvider {
  Updates getUpdates(Integer lastUpdateId);
  void sendMessage(Chat chat, String text);
  void replyToMessage(Chat chat, String text, Message message);
  void sendReplyKeyboard(Chat chat, String text, ReplyKeyboardMarkup keyboardMarkup);
  void hideKeyboard(Chat chat, String text, ReplyKeyboardRemove replyKeyboardRemove);
  void answerInlineQuery(String inlineQueryId, List<InlineQueryResult> inlineQueryResults);
  int getChatMembersCount(Chat chat);
}
