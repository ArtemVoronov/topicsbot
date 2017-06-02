package com.topicsbot.services.api.telegram.handlers.user;

import com.topicsbot.model.TimeZones;
import com.topicsbot.model.chat.Chat;
import com.topicsbot.services.api.telegram.TelegramApiProvider;
import com.topicsbot.services.api.telegram.handlers.KeyboardMaster;
import com.topicsbot.services.api.telegram.handlers.UpdateHandler;
import com.topicsbot.services.api.telegram.model.*;
import com.topicsbot.services.db.dao.ChatDAO;
import com.topicsbot.services.i18n.ResourceBundleService;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: Artem Voronov
 */
public class ShowSettingsKeyboardHandler implements UpdateHandler {

  private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
  private final TelegramApiProvider telegramApiProvider;
  private final ChatDAO chatDAO;
  private final ResourceBundleService resourceBundleService;

  public ShowSettingsKeyboardHandler(TelegramApiProvider telegramApiProvider, ChatDAO chatDAO,
                                     ResourceBundleService resourceBundleService) {
    this.telegramApiProvider = telegramApiProvider;
    this.chatDAO = chatDAO;
    this.resourceBundleService = resourceBundleService;
  }

  @Override
  public void handle(Update update) {
    Message message = update.getMessage();

    if (message == null)
      return;

    Chat chat = chatDAO.find(message.getChatId());
    String text = getChatSettingsMessage(chat);
    ReplyKeyboardMarkup keyboard = createSettingsKeyboard(chat);
    telegramApiProvider.sendReplyKeyboard(message.getChat(), text, keyboard);
  }

  private ReplyKeyboardMarkup createSettingsKeyboard(Chat chat) {
    String close = resourceBundleService.getMessage(chat.getLanguageShort(), "close.settings.button");
    String language = resourceBundleService.getMessage(chat.getLanguageShort(), "lang.button");
    String timezone = resourceBundleService.getMessage(chat.getLanguageShort(), "time.button");
    List<KeyboardRow> rowList = new ArrayList<>(2);
    KeyboardRow r1 = new KeyboardRow();
    r1.add(new KeyboardButton(KeyboardMaster.SECRET_COMMAND_CLOSE_SETTINGS + " " + close));
    KeyboardRow r2 = new KeyboardRow();
    r2.add(new KeyboardButton(KeyboardMaster.SECRET_COMMAND_LANG + " " + language));
    r2.add(new KeyboardButton(KeyboardMaster.SECRET_COMMAND_TIME + " " + timezone));
    rowList.add(r1);
    rowList.add(r2);

    return createShowKeyboard(rowList);
  }

  private static ReplyKeyboardMarkup createShowKeyboard(List<KeyboardRow> rowList) { //TODO: duplicate
    ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
    keyboardMarkup.setKeyboard(rowList);
    keyboardMarkup.setOneTimeKeyboard(true);
    keyboardMarkup.setResizeKeyboard(true);
    keyboardMarkup.setSelective(true);
    return keyboardMarkup;
  }

  private String getChatSettingsMessage(Chat chat) { //TODO: duplicate
    String template = resourceBundleService.getMessage(chat.getLanguageShort(), "settings.message");
    String language = chat.getLanguage().getName();
    String prettyTimezone = TimeZones.mappingFrom.get(chat.getTimezone().toString());
    String currentTime = LocalTime.now(chat.getTimezone()).format(TIME_FORMATTER);
    return String.format(template, language, prettyTimezone, currentTime);
  }
}
