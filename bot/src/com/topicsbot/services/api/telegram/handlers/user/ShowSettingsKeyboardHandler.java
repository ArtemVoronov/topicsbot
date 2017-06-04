package com.topicsbot.services.api.telegram.handlers.user;

import com.topicsbot.model.TimeZones;
import com.topicsbot.model.chat.Chat;
import com.topicsbot.services.api.telegram.TelegramApiProvider;
import com.topicsbot.services.api.telegram.handlers.KeyboardFactory;
import com.topicsbot.services.api.telegram.handlers.UpdateHandler;
import com.topicsbot.services.api.telegram.model.*;
import com.topicsbot.services.db.dao.ChatDAO;
import com.topicsbot.services.i18n.ResourceBundleService;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Author: Artem Voronov
 */
public class ShowSettingsKeyboardHandler implements UpdateHandler {

  private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
  private final TelegramApiProvider telegramApiProvider;
  private final ChatDAO chatDAO;
  private final ResourceBundleService resourceBundleService;
  private final KeyboardFactory keyboardFactory;

  public ShowSettingsKeyboardHandler(TelegramApiProvider telegramApiProvider, ChatDAO chatDAO,
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
    String text = getChatSettingsMessage(chat);
    ReplyKeyboardMarkup keyboard = keyboardFactory.createSettingsKeyboard(chat);
    telegramApiProvider.sendReplyKeyboard(message.getChat(), text, keyboard);
  }

  private String getChatSettingsMessage(Chat chat) { //TODO: duplicate
    String template = resourceBundleService.getMessage(chat.getLanguageShort(), "settings.message");
    String language = chat.getLanguage().getName();
    String prettyTimezone = TimeZones.mappingFrom.get(chat.getTimezone().toString());
    String currentTime = LocalTime.now(chat.getTimezone()).format(TIME_FORMATTER);
    return String.format(template, language, prettyTimezone, currentTime);
  }
}
