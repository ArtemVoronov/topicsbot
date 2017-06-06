package com.topicsbot.services.api.telegram;

import com.topicsbot.services.api.telegram.model.*;

import java.util.List;

/**
 * Author: Artem Voronov
 */
public interface TelegramApiProvider {
  Updates getUpdates(Integer lastUpdateId);
  void sendMessage(Chat chat, String text);
  void replyToMessage(Chat chat, String text, Message replyMessage);
  void sendReplyKeyboard(Chat chat, String text, Message replyMessage, ReplyKeyboardMarkup keyboardMarkup);
  void hideKeyboard(Chat chat, String text, Message replyMessage, ReplyKeyboardRemove replyKeyboardRemove);
  void answerInlineQuery(String inlineQueryId, List<InlineQueryResult> inlineQueryResults);
  int getChatMembersCount(String chatExternalId);
  Chat getChat(String externalId);
}
