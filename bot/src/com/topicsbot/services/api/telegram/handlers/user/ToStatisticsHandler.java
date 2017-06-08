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
import org.apache.log4j.Logger;
import org.hibernate.exception.ConstraintViolationException;

/**
 * Author: Artem Voronov
 */
public class ToStatisticsHandler implements UpdateHandler {
  private static final Logger logger = Logger.getLogger("PROCESS_UPDATES_DAEMON");

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

    String text = message.getText();

    if (text == null)
      return;

    text = text.trim();
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

    if (user == null)
      throw new IllegalStateException("Missed user record: " + userId + "-TELEGRAM");

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
    try {
      User user = userDAO.find(userId, ChannelType.TELEGRAM);

      if (user == null)
        user = userDAO.create(userId, userName, ChannelType.TELEGRAM);

      return user;
    } catch (ConstraintViolationException ex) {
      if (logger.isDebugEnabled())
        logger.debug("[TO_STATISTICS] Duplicate user entry create: " + userId + "-TELEGRAM");

      return userDAO.find(userId, ChannelType.TELEGRAM);
    }
  }
}
