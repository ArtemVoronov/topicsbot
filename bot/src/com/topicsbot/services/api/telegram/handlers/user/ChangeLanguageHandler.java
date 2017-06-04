package com.topicsbot.services.api.telegram.handlers.user;

import com.topicsbot.model.chat.Chat;
import com.topicsbot.model.chat.ChatLanguage;
import com.topicsbot.services.api.telegram.TelegramApiProvider;
import com.topicsbot.services.api.telegram.handlers.KeyboardFactory;
import com.topicsbot.services.api.telegram.handlers.UpdateHandler;
import com.topicsbot.services.api.telegram.model.Message;
import com.topicsbot.services.api.telegram.model.Update;
import com.topicsbot.services.db.dao.ChatDAO;
import com.topicsbot.services.i18n.ResourceBundleService;

/**
 * Author: Artem Voronov
 */
public class ChangeLanguageHandler implements UpdateHandler {

  private final ChatDAO chatDAO;
  private final TelegramApiProvider telegramApiProvider;
  private final ResourceBundleService resourceBundleService;


  public ChangeLanguageHandler(ChatDAO chatDAO,
                               TelegramApiProvider telegramApiProvider, ResourceBundleService resourceBundleService) {
    this.chatDAO = chatDAO;
    this.telegramApiProvider = telegramApiProvider;
    this.resourceBundleService = resourceBundleService;
  }

  @Override
  public void handle(Update update) {
    Message message = update.getMessage();

    if (message == null)
      return;

    Chat chat = chatDAO.find(message.getChatId());

    String text = message.getText().trim();
    String[] tokens = text.split(KeyboardFactory.SECRET_COMMAND_LANG_PARAM);
    if (tokens.length < 2)
      return;

    String chosenLang = tokens[1].trim();
    ChatLanguage newLanguage = ChatLanguage.mappingByName.get(chosenLang);

    chatDAO.update(chat.getExternalId(), newLanguage);

    String template = resourceBundleService.getMessage(newLanguage.name().toLowerCase(), "lang.was.chosen.message");
    String feedback = template + chosenLang;
    telegramApiProvider.hideKeyboard(message.getChat(), feedback, KeyboardFactory.createHideKeyboard());
  }
}
