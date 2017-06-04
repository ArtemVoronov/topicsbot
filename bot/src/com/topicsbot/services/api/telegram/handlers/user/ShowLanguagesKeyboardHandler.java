package com.topicsbot.services.api.telegram.handlers.user;

import com.topicsbot.model.chat.Chat;
import com.topicsbot.services.api.telegram.TelegramApiProvider;
import com.topicsbot.services.api.telegram.handlers.KeyboardFactory;
import com.topicsbot.services.api.telegram.handlers.UpdateHandler;
import com.topicsbot.services.api.telegram.model.*;
import com.topicsbot.services.db.dao.ChatDAO;
import com.topicsbot.services.i18n.ResourceBundleService;


/**
 * Author: Artem Voronov
 */
public class ShowLanguagesKeyboardHandler implements UpdateHandler {

  private final TelegramApiProvider telegramApiProvider;
  private final ChatDAO chatDAO;
  private final ResourceBundleService resourceBundleService;
  private final KeyboardFactory keyboardFactory;

  public ShowLanguagesKeyboardHandler(TelegramApiProvider telegramApiProvider, ChatDAO chatDAO,
                                      ResourceBundleService resourceBundleService, KeyboardFactory keyboardFactory) {
    this.telegramApiProvider = telegramApiProvider;
    this.chatDAO = chatDAO;
    this.resourceBundleService = resourceBundleService;
    this.keyboardFactory = keyboardFactory;
  }

  @Override
  public void handle(Update update) {
    Message message = update.getMessage();

    if (message == null)
      return;

    Chat chat = chatDAO.find(message.getChatId());
    String text = resourceBundleService.getMessage(chat.getLanguageShort(), "choose.lang.message");
    ReplyKeyboardMarkup keyboard = keyboardFactory.createLanguageKeyboard(chat);
    telegramApiProvider.sendReplyKeyboard(message.getChat(), text, keyboard);
  }

}
