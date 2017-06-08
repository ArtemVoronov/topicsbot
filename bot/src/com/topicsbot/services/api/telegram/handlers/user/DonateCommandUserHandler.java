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
public class DonateCommandUserHandler extends CommonUserHandler implements UpdateHandler {

  private final TelegramApiProvider telegramApiProvider;
  private final ChatDAO chatDAO;
  private final ResourceBundleService resourceBundleService;

  public DonateCommandUserHandler(TelegramApiProvider telegramApiProvider, ResourceBundleService resourceBundleService,
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
    String template = resourceBundleService.getMessage(chat.getLanguageShort(), "donate.message");
    String result = template + "\nhttp://www.topicsbot.com/donate";
    telegramApiProvider.sendMessage(message.getChat(), result);

    updateChatCounters(chat, CounterType.DONATE_COMMAND, 1);
    updateUserCounter(message, chat, CounterType.DONATE_COMMAND, 1);
  }
}
