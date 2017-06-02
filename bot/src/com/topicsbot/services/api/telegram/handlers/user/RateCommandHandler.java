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
public class RateCommandHandler implements UpdateHandler {

  private static final char STAR = '\u2B50';
  private static final String FIVE_STARS = "" + STAR + STAR + STAR + STAR + STAR;
  private final TelegramApiProvider telegramApiProvider;
  private final ChatDAO chatDAO;
  private final ResourceBundleService resourceBundleService;

  public RateCommandHandler(TelegramApiProvider telegramApiProvider, ResourceBundleService resourceBundleService, ChatDAO chatDAO) {
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
    String template = resourceBundleService.getMessage(chat.getLanguageShort(), "rate.message");
    String result = String.format(template, FIVE_STARS);
    telegramApiProvider.sendMessage(message.getChat(), result);
  }
}
