package com.topicsbot.services.api.telegram.handlers.user;

import com.topicsbot.model.TimeZones;
import com.topicsbot.model.chat.Chat;
import com.topicsbot.services.api.telegram.TelegramApiProvider;
import com.topicsbot.services.api.telegram.handlers.UpdateHandler;
import com.topicsbot.services.api.telegram.model.Message;
import com.topicsbot.services.api.telegram.model.ReplyKeyboardRemove;
import com.topicsbot.services.api.telegram.model.Update;
import com.topicsbot.services.db.dao.ChatDAO;
import com.topicsbot.services.i18n.ResourceBundleService;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Author: Artem Voronov
 */
public class HideSettingsKeyboardHandler implements UpdateHandler {

  public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
  private final TelegramApiProvider telegramApiProvider;
  private final ChatDAO chatDAO;
  private final ResourceBundleService resourceBundleService;

  public HideSettingsKeyboardHandler(TelegramApiProvider telegramApiProvider, ChatDAO chatDAO,
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
    ReplyKeyboardRemove keyboardRemove = createHideKeyboard();
    telegramApiProvider.hideKeyboard(message.getChat(), text, keyboardRemove);
  }

  private static ReplyKeyboardRemove createHideKeyboard() {
    ReplyKeyboardRemove replyKeyboardRemove = new ReplyKeyboardRemove();
    replyKeyboardRemove.setRemoveKeyboard(true);
    replyKeyboardRemove.setSelective(true);
    return replyKeyboardRemove;
  }

  private String getChatSettingsMessage(Chat chat) { //TODO: duplication
    String template = resourceBundleService.getMessage(chat.getLanguageShort(), "settings.message");
    String language = chat.getLanguage().getName();
    String prettyTimezone = TimeZones.mappingFrom.get(chat.getTimezone().toString());
    String currentTime = LocalTime.now(chat.getTimezone()).format(TIME_FORMATTER);
    return String.format(template, language, prettyTimezone, currentTime);
  }
}