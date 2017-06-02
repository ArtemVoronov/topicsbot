package com.topicsbot.services.api.telegram.handlers.user;

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
public class ShowLanguagesKeyboardHandler implements UpdateHandler {


  private static final String ENGLISH = "English";
  private static final String RUSSIAN = "Русский";
  private static final String POLISH = "Polski";
  private static final String PORTUGUESE = "Português";
  private static final String SPANISH = "Español";
  private static final String GERMAN = "Deutsch";
  private static final String FRENCH = "Français";
  private static final String ITALIAN = "Italiano";
  private static final String JAPANESE = "日本語";
  private static final String CHINESE = "中文";
  private static final String ARABIC = "العربية";
  private static final String HINDI = "हिन्दी";
  private static final String CZECH = "Čeština";
  private static final String TURKISH = "Türkçe";
  private static final String KOREAN = "한국어";
  private final TelegramApiProvider telegramApiProvider;
  private final ChatDAO chatDAO;
  private final ResourceBundleService resourceBundleService;

  public ShowLanguagesKeyboardHandler(TelegramApiProvider telegramApiProvider, ChatDAO chatDAO,
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
    String text = resourceBundleService.getMessage(chat.getLanguageShort(), "choose.lang.message");
    ReplyKeyboardMarkup keyboard = createLanguageKeyboard(chat);
    telegramApiProvider.sendReplyKeyboard(message.getChat(), text, keyboard);
  }

  private ReplyKeyboardMarkup createLanguageKeyboard(Chat chat) {
    String back = resourceBundleService.getMessage(chat.getLanguageShort(), "back.button");
    List<KeyboardRow> rowList = new ArrayList<>(2);
    KeyboardRow r1 = new KeyboardRow();
    r1.add(new KeyboardButton(KeyboardMaster.SECRET_COMMAND_SETTINGS + " " + back));
    KeyboardRow r2 = new KeyboardRow();
    r2.add(new KeyboardButton(KeyboardMaster.SECRET_COMMAND_LANG_PARAM + " " + ENGLISH));
    r2.add(new KeyboardButton(KeyboardMaster.SECRET_COMMAND_LANG_PARAM + " " + RUSSIAN));
    r2.add(new KeyboardButton(KeyboardMaster.SECRET_COMMAND_LANG_PARAM + " " + PORTUGUESE));
    r2.add(new KeyboardButton(KeyboardMaster.SECRET_COMMAND_LANG_PARAM + " " + SPANISH));
    r2.add(new KeyboardButton(KeyboardMaster.SECRET_COMMAND_LANG_PARAM + " " + FRENCH));
    KeyboardRow r3 = new KeyboardRow();
    r3.add(new KeyboardButton(KeyboardMaster.SECRET_COMMAND_LANG_PARAM + " " + GERMAN));
    r3.add(new KeyboardButton(KeyboardMaster.SECRET_COMMAND_LANG_PARAM + " " + ITALIAN));
    r3.add(new KeyboardButton(KeyboardMaster.SECRET_COMMAND_LANG_PARAM + " " + POLISH));
    r3.add(new KeyboardButton(KeyboardMaster.SECRET_COMMAND_LANG_PARAM + " " + CZECH));
    r3.add(new KeyboardButton(KeyboardMaster.SECRET_COMMAND_LANG_PARAM + " " + TURKISH));

    KeyboardRow r4 = new KeyboardRow();
    r4.add(new KeyboardButton(KeyboardMaster.SECRET_COMMAND_LANG_PARAM + " " + ARABIC));
    r4.add(new KeyboardButton(KeyboardMaster.SECRET_COMMAND_LANG_PARAM + " " + HINDI));
    r4.add(new KeyboardButton(KeyboardMaster.SECRET_COMMAND_LANG_PARAM + " " + JAPANESE));
    r4.add(new KeyboardButton(KeyboardMaster.SECRET_COMMAND_LANG_PARAM + " " + CHINESE));
    r4.add(new KeyboardButton(KeyboardMaster.SECRET_COMMAND_LANG_PARAM + " " + KOREAN));
    rowList.add(r1);
    rowList.add(r2);
    rowList.add(r3);
    rowList.add(r4);
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
