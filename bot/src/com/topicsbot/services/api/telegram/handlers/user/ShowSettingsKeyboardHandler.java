package com.topicsbot.services.api.telegram.handlers.user;

import com.topicsbot.model.chat.Chat;
import com.topicsbot.services.api.telegram.TelegramApiProvider;
import com.topicsbot.services.api.telegram.handlers.KeyboardFactory;
import com.topicsbot.services.api.telegram.handlers.UpdateHandler;
import com.topicsbot.services.api.telegram.model.Message;
import com.topicsbot.services.api.telegram.model.ReplyKeyboardMarkup;
import com.topicsbot.services.api.telegram.model.Update;
import com.topicsbot.services.db.dao.ChatDAO;
import com.topicsbot.services.messages.MessagesFactory;

/**
 * Author: Artem Voronov
 */
public class ShowSettingsKeyboardHandler implements UpdateHandler {

  private final TelegramApiProvider telegramApiProvider;
  private final ChatDAO chatDAO;
  private final KeyboardFactory keyboardFactory;
  private final MessagesFactory messagesFactory;

  public ShowSettingsKeyboardHandler(TelegramApiProvider telegramApiProvider, MessagesFactory messagesFactory,
                                     ChatDAO chatDAO, KeyboardFactory keyboardFactory) {
    this.telegramApiProvider = telegramApiProvider;
    this.chatDAO = chatDAO;
    this.messagesFactory = messagesFactory;
    this.keyboardFactory = keyboardFactory;
  }

  @Override
  public void handle(Update update) {
    Message message = update.getMessage();

    if (message == null)
      return;

    Chat chat = chatDAO.find(message.getChatId());
    String text = messagesFactory.getChatSettingsMessage(chat);
    ReplyKeyboardMarkup keyboard = keyboardFactory.createSettingsKeyboard(chat);
    telegramApiProvider.sendReplyKeyboard(message.getChat(), text, message, keyboard);
  }
}
