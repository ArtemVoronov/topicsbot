package com.topicsbot.services.api.telegram.handlers;

import com.topicsbot.model.TimeZones;
import com.topicsbot.model.chat.Chat;
import com.topicsbot.model.chat.ChatLanguage;
import com.topicsbot.services.api.telegram.model.KeyboardButton;
import com.topicsbot.services.api.telegram.model.KeyboardRow;
import com.topicsbot.services.api.telegram.model.ReplyKeyboardMarkup;
import com.topicsbot.services.api.telegram.model.ReplyKeyboardRemove;
import com.topicsbot.services.i18n.ResourceBundleService;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: Artem Voronov
 */
public class KeyboardFactory {
  //TODO: private!
  private static final char HIDDEN_CHAR = '\u2063';
  public static final String SECRET_COMMAND_LANG_PARAM = HIDDEN_CHAR + "";
  public static final String SECRET_COMMAND_TIME_PARAM = HIDDEN_CHAR + " " + SECRET_COMMAND_LANG_PARAM;
  public static final String SECRET_COMMAND_LANG = HIDDEN_CHAR + " " + SECRET_COMMAND_TIME_PARAM;
  public static final String SECRET_COMMAND_TIME = HIDDEN_CHAR + " " + SECRET_COMMAND_LANG;
  public static final String SECRET_COMMAND_CLOSE_SETTINGS = HIDDEN_CHAR + " " + SECRET_COMMAND_TIME;
  public static final String SECRET_COMMAND_SETTINGS = HIDDEN_CHAR + " " + SECRET_COMMAND_CLOSE_SETTINGS;

  private final ResourceBundleService resourceBundleService;

  public KeyboardFactory(ResourceBundleService resourceBundleService) {
    this.resourceBundleService = resourceBundleService;
  }

  public ReplyKeyboardMarkup createTimezonesKeyboard(Chat chat) {
    String back = resourceBundleService.getMessage(chat.getLanguageShort(), "back.button");
    int rowsSize = 9;
    List<KeyboardRow> rowList = new ArrayList<>(rowsSize);
    KeyboardRow[] rows = new KeyboardRow[rowsSize];
    for (int i = 0; i < rowsSize; i++) {
      rows[i] = new KeyboardRow();
    }

    rows[0].add(new KeyboardButton(KeyboardFactory.SECRET_COMMAND_SETTINGS + " " + back));

    int count1 = 1;
    int count2 = 1;
    rows[1].add(new KeyboardButton(KeyboardFactory.SECRET_COMMAND_TIME_PARAM + " " + TimeZones.GMT_0));
    for (int i = 2 ; i < 8; i++) {
      rows[i].add(new KeyboardButton(KeyboardFactory.SECRET_COMMAND_TIME_PARAM + " " + TimeZones.valueOf("GMT_PLUS_" + count1++)));
      rows[i].add(new KeyboardButton(KeyboardFactory.SECRET_COMMAND_TIME_PARAM + " " + TimeZones.valueOf("GMT_PLUS_" + count1++)));
      rows[i].add(new KeyboardButton(KeyboardFactory.SECRET_COMMAND_TIME_PARAM + " " + TimeZones.valueOf("GMT_MINUS_" + count2++)));
      rows[i].add(new KeyboardButton(KeyboardFactory.SECRET_COMMAND_TIME_PARAM + " " + TimeZones.valueOf("GMT_MINUS_" + count2++)));
    }
    rows[8].add(new KeyboardButton(KeyboardFactory.SECRET_COMMAND_TIME_PARAM + " " + TimeZones.GMT_MINUS_13));
    for(KeyboardRow r : rows) {
      rowList.add(r);
    }
    return KeyboardFactory.createShowKeyboard(rowList);
  }

  public ReplyKeyboardMarkup createSettingsKeyboard(Chat chat) {
    String close = resourceBundleService.getMessage(chat.getLanguageShort(), "close.settings.button");
    String language = resourceBundleService.getMessage(chat.getLanguageShort(), "lang.button");
    String timezone = resourceBundleService.getMessage(chat.getLanguageShort(), "time.button");
    List<KeyboardRow> rowList = new ArrayList<>(2);
    KeyboardRow r1 = new KeyboardRow();
    r1.add(new KeyboardButton(KeyboardFactory.SECRET_COMMAND_CLOSE_SETTINGS + " " + close));
    KeyboardRow r2 = new KeyboardRow();
    r2.add(new KeyboardButton(KeyboardFactory.SECRET_COMMAND_LANG + " " + language));
    r2.add(new KeyboardButton(KeyboardFactory.SECRET_COMMAND_TIME + " " + timezone));
    rowList.add(r1);
    rowList.add(r2);

    return KeyboardFactory.createShowKeyboard(rowList);
  }

  public ReplyKeyboardMarkup createLanguageKeyboard(Chat chat) {
    String back = resourceBundleService.getMessage(chat.getLanguageShort(), "back.button");
    List<KeyboardRow> rowList = new ArrayList<>(2);
    KeyboardRow r1 = new KeyboardRow();
    r1.add(new KeyboardButton(KeyboardFactory.SECRET_COMMAND_SETTINGS + " " + back));
    KeyboardRow r2 = new KeyboardRow();
    r2.add(new KeyboardButton(KeyboardFactory.SECRET_COMMAND_LANG_PARAM + " " + ChatLanguage.EN.getName()));
    r2.add(new KeyboardButton(KeyboardFactory.SECRET_COMMAND_LANG_PARAM + " " + ChatLanguage.RU.getName()));
    r2.add(new KeyboardButton(KeyboardFactory.SECRET_COMMAND_LANG_PARAM + " " + ChatLanguage.PT.getName()));
    r2.add(new KeyboardButton(KeyboardFactory.SECRET_COMMAND_LANG_PARAM + " " + ChatLanguage.ES.getName()));
    r2.add(new KeyboardButton(KeyboardFactory.SECRET_COMMAND_LANG_PARAM + " " + ChatLanguage.FR.getName()));
    KeyboardRow r3 = new KeyboardRow();
    r3.add(new KeyboardButton(KeyboardFactory.SECRET_COMMAND_LANG_PARAM + " " + ChatLanguage.DE.getName()));
    r3.add(new KeyboardButton(KeyboardFactory.SECRET_COMMAND_LANG_PARAM + " " + ChatLanguage.IT.getName()));
    r3.add(new KeyboardButton(KeyboardFactory.SECRET_COMMAND_LANG_PARAM + " " + ChatLanguage.PL.getName()));
    r3.add(new KeyboardButton(KeyboardFactory.SECRET_COMMAND_LANG_PARAM + " " + ChatLanguage.CS.getName()));
    r3.add(new KeyboardButton(KeyboardFactory.SECRET_COMMAND_LANG_PARAM + " " + ChatLanguage.TR.getName()));

    KeyboardRow r4 = new KeyboardRow();
    r4.add(new KeyboardButton(KeyboardFactory.SECRET_COMMAND_LANG_PARAM + " " + ChatLanguage.AR.getName()));
    r4.add(new KeyboardButton(KeyboardFactory.SECRET_COMMAND_LANG_PARAM + " " + ChatLanguage.HI.getName()));
    r4.add(new KeyboardButton(KeyboardFactory.SECRET_COMMAND_LANG_PARAM + " " + ChatLanguage.JA.getName()));
    r4.add(new KeyboardButton(KeyboardFactory.SECRET_COMMAND_LANG_PARAM + " " + ChatLanguage.ZH.getName()));
    r4.add(new KeyboardButton(KeyboardFactory.SECRET_COMMAND_LANG_PARAM + " " + ChatLanguage.KO.getName()));
    rowList.add(r1);
    rowList.add(r2);
    rowList.add(r3);
    rowList.add(r4);
    return KeyboardFactory.createShowKeyboard(rowList);
  }

  public static ReplyKeyboardRemove createHideKeyboard() {
    ReplyKeyboardRemove replyKeyboardRemove = new ReplyKeyboardRemove();
    replyKeyboardRemove.setRemoveKeyboard(true);
    replyKeyboardRemove.setSelective(true);
    return replyKeyboardRemove;
  }

  public static ReplyKeyboardMarkup createShowKeyboard(List<KeyboardRow> rowList) {
    ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
    keyboardMarkup.setKeyboard(rowList);
    keyboardMarkup.setOneTimeKeyboard(true);
    keyboardMarkup.setResizeKeyboard(true);
    keyboardMarkup.setSelective(true);
    return keyboardMarkup;
  }
}
