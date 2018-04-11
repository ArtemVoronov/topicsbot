package com.topicsbot.services.api.telegram.handlers.user;

import com.topicsbot.model.chat.Chat;
import com.topicsbot.model.statistics.ChatDayStatistics;
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
public class RankCommandUserHandler extends CommonUserHandler implements UpdateHandler {

  private final TelegramApiProvider telegramApiProvider;
  private final ChatDAO chatDAO;
  private final ResourceBundleService resourceBundleService;

  public RankCommandUserHandler(TelegramApiProvider telegramApiProvider, ResourceBundleService resourceBundleService,
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
    ChatDayStatistics chatStatistics = cache.getChatStatistics(chat.getExternalId(), chat.getRebirthDate());

    double averageFlood = chat.getAverageFlood();

    if (averageFlood <= 0) {
      String feedback = resourceBundleService.getMessage(chat.getLanguageShort(), "error.message");
      telegramApiProvider.sendMessage(message.getChat(), feedback);
      return;
    }

    String rank;
    double currentFloodProgress;
    if (chatStatistics != null) {
      currentFloodProgress = chatStatistics.getFloodSize() / averageFlood * 100;
      rank = getFloodRank(chat, currentFloodProgress);
    } else {
      currentFloodProgress = 0;
      rank = resourceBundleService.getMessage(chat.getLanguageShort(), "gods.cursing");
    }

    String template = resourceBundleService.getMessage(chat.getLanguageShort(), "rank.message");
    String rankMessage = String.format(template, currentFloodProgress);
    String result = rank + ". " + rankMessage;
    telegramApiProvider.sendMessage(message.getChat(), result);

    updateChatCounters(chat, CounterType.RANK_COMMAND, 1);
    updateUserCounter(message, chat, CounterType.RANK_COMMAND, 1);
  }

  private String getFloodRank(Chat chat, double currentFloodProgress) {
    if (currentFloodProgress < 95.0) {
      return resourceBundleService.getMessage(chat.getLanguageShort(), "gods.cursing");
    } else if (currentFloodProgress >= 95.0 && currentFloodProgress <= 105.0) {
      return resourceBundleService.getMessage(chat.getLanguageShort(), "norma");
    } else {
      return resourceBundleService.getMessage(chat.getLanguageShort(), "gods.blessing");
    }
  }
}
