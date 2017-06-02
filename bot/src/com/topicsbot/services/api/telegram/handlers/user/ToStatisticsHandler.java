package com.topicsbot.services.api.telegram.handlers.user;

import com.topicsbot.model.ChannelType;
import com.topicsbot.model.chat.Chat;
import com.topicsbot.model.statistics.ChatDayStatistics;
import com.topicsbot.model.statistics.UserDayStatistics;
import com.topicsbot.model.user.User;
import com.topicsbot.services.analysis.AnalysisProvider;
import com.topicsbot.services.api.telegram.handlers.UpdateHandler;
import com.topicsbot.services.api.telegram.model.Message;
import com.topicsbot.services.api.telegram.model.Update;
import com.topicsbot.services.cache.CacheService;
import com.topicsbot.services.db.dao.ChatDAO;
import com.topicsbot.services.db.dao.UserDAO;

/**
 * Author: Artem Voronov
 */
public class ToStatisticsHandler implements UpdateHandler {

  private final AnalysisProvider analysisProvider;
  private final CacheService cache;
  private final ChatDAO chatDAO;
  private final UserDAO userDAO;

  public ToStatisticsHandler(AnalysisProvider analysisProvider, CacheService cache,
                             ChatDAO chatDAO, UserDAO userDAO) {
    this.analysisProvider = analysisProvider;
    this.cache = cache;
    this.chatDAO = chatDAO;
    this.userDAO = userDAO;
  }

  @Override
  public void handle(Update update) {
    Message message = update.getMessage();

    if (message == null)
      return;

    Chat chat = chatDAO.find(message.getChatId());
    String text = message.getText().trim();
    analysisProvider.index(text, chat);

    updateChatDayStatistics(message, chat);
    updateUserDayStatistics(message, chat);
  }

  private static int getWordCounter(Message message) {
    return !message.hasText() ? 0 : message.getText().split(" ").length;
  }

  private static int getFloodSize(Message message) {
    return !message.hasText() ? 0 : message.getText().replaceAll("\\s+", "").length();
  }

  private void updateChatDayStatistics(Message message, Chat chat) {
    ChatDayStatistics statistics = cache.getChatStatistics(chat);

    int words = getWordCounter(message);
    int flood = getFloodSize(message);

    if (statistics == null) {
      cache.createChatStatistics(chat, flood, 1, words);
    } else {
      cache.updateStatistics(statistics, flood, 1, words);
    }

    //TODO: stickers, images, videos, audios
  }

  private void updateUserDayStatistics(Message message, Chat chat) {
    String userId = message.getUserId();
    String userName = message.getUserName();
    User user = getOrCreateUser(userId, userName);

    UserDayStatistics statistics = cache.getUserStatistics(chat, user);

    int words = getWordCounter(message);
    int flood = getFloodSize(message);

    if (statistics == null) {
      cache.createUserStatistics(chat, user, flood, 1, words);
    } else {
      cache.updateStatistics(statistics, flood, 1, words);
    }

    //TODO: stickers, images, videos, audios
  }

  private User getOrCreateUser(String userId, String userName) {//TODO: duplicate operation (AddTopicHandler)
    User user = userDAO.find(userId, ChannelType.TELEGRAM);

    if (user == null) {
      user = userDAO.create(userId, userName, ChannelType.TELEGRAM);
    }

    return user;
  }
}
