package com.topicsbot.services.api.telegram.handlers.user;

import com.topicsbot.model.TimeZones;
import com.topicsbot.model.chat.Chat;
import com.topicsbot.services.api.telegram.TelegramApiProvider;
import com.topicsbot.services.api.telegram.handlers.KeyboardMaster;
import com.topicsbot.services.api.telegram.handlers.UpdateHandler;
import com.topicsbot.services.api.telegram.model.*;
import com.topicsbot.services.db.dao.ChatDAO;
import com.topicsbot.services.i18n.ResourceBundleService;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: Artem Voronov
 */
public class ShowTimezonesKeyboardHandler implements UpdateHandler {
  private final TelegramApiProvider telegramApiProvider;
  private final ChatDAO chatDAO;
  private final ResourceBundleService resourceBundleService;

  public ShowTimezonesKeyboardHandler(TelegramApiProvider telegramApiProvider, ChatDAO chatDAO,
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
    String text = resourceBundleService.getMessage(chat.getLanguageShort(), "choose.time.message");
    ReplyKeyboardMarkup keyboard = createTimezonesKeyboard(chat);
    telegramApiProvider.sendReplyKeyboard(message.getChat(), text, keyboard);
  }

  private ReplyKeyboardMarkup createTimezonesKeyboard(Chat chat) {
    String back = resourceBundleService.getMessage(chat.getLanguageShort(), "back.button");
    int rowsSize = 9;
    List<KeyboardRow> rowList = new ArrayList<>(rowsSize);
    KeyboardRow[] rows = new KeyboardRow[rowsSize];
    for (int i = 0; i < rowsSize; i++) {
      rows[i] = new KeyboardRow();
    }

    rows[0].add(new KeyboardButton(KeyboardMaster.SECRET_COMMAND_SETTINGS + " " + back));

    int count1 = 1;
    int count2 = 1;
    rows[1].add(new KeyboardButton(KeyboardMaster.SECRET_COMMAND_TIME_PARAM + " " + TimeZones.GMT_0));
    for (int i = 2 ; i < 8; i++) {
      rows[i].add(new KeyboardButton(KeyboardMaster.SECRET_COMMAND_TIME_PARAM + " " + TimeZones.valueOf("GMT_PLUS_" + count1++)));
      rows[i].add(new KeyboardButton(KeyboardMaster.SECRET_COMMAND_TIME_PARAM + " " + TimeZones.valueOf("GMT_PLUS_" + count1++)));
      rows[i].add(new KeyboardButton(KeyboardMaster.SECRET_COMMAND_TIME_PARAM + " " + TimeZones.valueOf("GMT_MINUS_" + count2++)));
      rows[i].add(new KeyboardButton(KeyboardMaster.SECRET_COMMAND_TIME_PARAM + " " + TimeZones.valueOf("GMT_MINUS_" + count2++)));
    }
    rows[8].add(new KeyboardButton(KeyboardMaster.SECRET_COMMAND_TIME_PARAM + " " + TimeZones.GMT_MINUS_13));
    for(KeyboardRow r : rows) {
      rowList.add(r);
    }
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
}
