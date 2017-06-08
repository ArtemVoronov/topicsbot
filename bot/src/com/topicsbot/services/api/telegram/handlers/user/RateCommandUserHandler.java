package com.topicsbot.services.api.telegram.handlers.user;

import com.topicsbot.model.chat.Chat;
import com.topicsbot.model.statistics.CounterType;
import com.topicsbot.services.api.telegram.TelegramApiProvider;
import com.topicsbot.services.api.telegram.handlers.UpdateHandler;
import com.topicsbot.services.api.telegram.model.Message;
import com.topicsbot.services.api.telegram.model.Update;
import com.topicsbot.services.cache.CacheService;
import com.topicsbot.services.db.dao.ChatDAO;
import com.topicsbot.services.db.dao.UserDAO;
import com.topicsbot.services.i18n.ResourceBundleService;

/**
 * Author: Artem Voronov
 */
public class RateCommandUserHandler extends CommonUserHandler implements UpdateHandler {

  private static final char STAR = '\u2B50';
  private static final String FIVE_STARS = "" + STAR + STAR + STAR + STAR + STAR;
  private final TelegramApiProvider telegramApiProvider;
  private final ChatDAO chatDAO;
  private final ResourceBundleService resourceBundleService;

  public RateCommandUserHandler(TelegramApiProvider telegramApiProvider, ResourceBundleService resourceBundleService,
                                CacheService cache, ChatDAO chatDAO, UserDAO userDAO) {
    super(cache, userDAO);
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

    updateChatCounters(chat, CounterType.RATE_COMMAND, 1);
    updateUserCounter(message, chat, CounterType.RATE_COMMAND, 1);
  }
}
