package com.topicsbot.services.api.telegram.handlers.user;

import com.topicsbot.BotContext;
import com.topicsbot.model.chat.Chat;
import com.topicsbot.model.chat.ChatLanguage;
import com.topicsbot.services.api.telegram.TelegramApiProvider;
import com.topicsbot.services.api.telegram.handlers.UpdateHandler;
import com.topicsbot.services.api.telegram.model.Message;
import com.topicsbot.services.api.telegram.model.Update;
import com.topicsbot.services.db.dao.ChatDAO;
import com.topicsbot.services.i18n.ResourceBundleService;

/**
 * Author: Artem Voronov
 */
public class StartCommandHandler implements UpdateHandler {

  private final TelegramApiProvider telegramApiProvider;
  private final ChatDAO chatDAO;
  private final ResourceBundleService resourceBundleService;

  public StartCommandHandler(TelegramApiProvider telegramApiProvider, ResourceBundleService resourceBundleService, ChatDAO chatDAO) {
    this.telegramApiProvider = telegramApiProvider;
    this.chatDAO = chatDAO;
    this.resourceBundleService = resourceBundleService;
  }

  @Override
  public void handle(Update update) {
    Message message = update.getMessage();
    Chat chat = chatDAO.find(message.getChatId());
    ChatLanguage language = chat.getLanguage();
    String build = resourceBundleService.getMessage(chat.getLanguageShort(), "build.note");
    String help = resourceBundleService.getMessage(chat.getLanguageShort(), "help.message");
    String startMessageTemplate = resourceBundleService.getMessage(chat.getLanguageShort(), "start.message");
    String result = String.format(startMessageTemplate, "http://topicsbot.com", BotContext.getInstance().getVersion(), build, help);
    telegramApiProvider.sendMessage(message.getChat(), result);
  }
}
