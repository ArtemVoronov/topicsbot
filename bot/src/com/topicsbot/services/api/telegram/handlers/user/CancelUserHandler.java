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
public class CancelUserHandler extends CommonUserHandler implements UpdateHandler {

  private final ChatDAO chatDAO;
  private final TelegramApiProvider telegramApiProvider;
  private final ResourceBundleService resourceBundleService;


  public CancelUserHandler(ChatDAO chatDAO, UserDAO userDAO,
                           TelegramApiProvider telegramApiProvider, ResourceBundleService resourceBundleService,
                           CacheService cache) {
    super(cache, userDAO);
    this.chatDAO = chatDAO;
    this.telegramApiProvider = telegramApiProvider;
    this.resourceBundleService = resourceBundleService;
  }

  @Override
  public void handle(Update update) {
    Message message = update.getMessage();

    if (message == null)
      return;

    cache.removeWaiter(message.getChatId(), message.getUserId());

    Chat chat = chatDAO.find(message.getChatId());

    String feedback = resourceBundleService.getMessage(chat.getLanguageShort(), "canceled.message");
    telegramApiProvider.sendMessage(message.getChat(), feedback);

    updateChatCounters(chat, CounterType.CANCEL_COMMAND, 1);
    updateUserCounter(message, chat, CounterType.CANCEL_COMMAND, 1);
  }

}
