package com.topicsbot.services.api.telegram.handlers.user;

import com.topicsbot.model.chat.Chat;
import com.topicsbot.model.chat.ChatLanguage;
import com.topicsbot.services.api.telegram.TelegramApiProvider;
import com.topicsbot.services.api.telegram.handlers.UpdateHandler;
import com.topicsbot.services.api.telegram.model.Message;
import com.topicsbot.services.api.telegram.model.Update;
import com.topicsbot.services.db.dao.ChatController;
import com.topicsbot.services.i18n.ResourceBundleService;

/**
 * Author: Artem Voronov
 */
public class StartCommandHandler implements UpdateHandler {

  private final TelegramApiProvider telegramApiProvider;
  private final ChatController chatController;
  private final ResourceBundleService resourceBundleService;

  public StartCommandHandler(TelegramApiProvider telegramApiProvider, ResourceBundleService resourceBundleService, ChatController chatController) {
    this.telegramApiProvider = telegramApiProvider;
    this.chatController = chatController;
    this.resourceBundleService = resourceBundleService;
  }

  @Override
  public void handle(Update update) {
    Message message = update.getMessage();
    Chat chat = chatController.find(message.getChatId());
    ChatLanguage language = chat.getLanguage();
    String text = resourceBundleService.getMessage(language.name().toLowerCase(), "build.note");//TODO
    telegramApiProvider.sendMessage(message.getChat(), text);
  }
}
