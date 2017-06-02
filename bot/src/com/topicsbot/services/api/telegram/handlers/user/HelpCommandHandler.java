package com.topicsbot.services.api.telegram.handlers.user;

import com.topicsbot.model.chat.Chat;
import com.topicsbot.services.api.telegram.TelegramApiProvider;
import com.topicsbot.services.api.telegram.handlers.UpdateHandler;
import com.topicsbot.services.api.telegram.model.Message;
import com.topicsbot.services.api.telegram.model.Update;
import com.topicsbot.services.db.dao.ChatDAO;
import com.topicsbot.services.i18n.ResourceBundleService;

/**
 * Author: Artem Voronov
 */
public class HelpCommandHandler implements UpdateHandler {

  private final TelegramApiProvider telegramApiProvider;
  private final ChatDAO chatDAO;
  private final ResourceBundleService resourceBundleService;

  public HelpCommandHandler(TelegramApiProvider telegramApiProvider, ResourceBundleService resourceBundleService, ChatDAO chatDAO) {
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
    String result = resourceBundleService.getMessage(chat.getLanguageShort(), "help.message");
    telegramApiProvider.sendMessage(message.getChat(), result);
  }
}
