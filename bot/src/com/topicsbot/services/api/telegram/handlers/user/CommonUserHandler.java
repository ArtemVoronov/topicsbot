package com.topicsbot.services.api.telegram.handlers.user;

import com.topicsbot.model.ChannelType;
import com.topicsbot.model.chat.Chat;
import com.topicsbot.model.statistics.ChatDayStatistics;
import com.topicsbot.model.statistics.CounterType;
import com.topicsbot.model.statistics.UserDayStatistics;
import com.topicsbot.model.user.User;
import com.topicsbot.services.api.telegram.model.Message;
import com.topicsbot.services.cache.CacheService;
import com.topicsbot.services.db.dao.UserDAO;
import org.apache.log4j.Logger;
import org.hibernate.exception.ConstraintViolationException;

/**
 * Author: Artem Voronov
 */
public class CommonUserHandler {
  private static final Logger logger = Logger.getLogger("PROCESS_UPDATES_DAEMON");

  protected final CacheService cache;
  protected final UserDAO userDAO;

  public CommonUserHandler(CacheService cache, UserDAO userDAO) {
    this.cache = cache;
    this.userDAO = userDAO;
  }

  protected void updateChatCounters(Chat chat, CounterType counterType, int counterValue) {
    ChatDayStatistics statistics = cache.getChatStatistics(chat);

    if (statistics == null) {
      cache.createChatStatistics(chat, counterType, counterValue);
    } else {
      cache.updateStatistics(statistics, counterType, counterValue);
    }
  }

  protected void updateUserCounter(Message message, Chat chat, CounterType counterType, int counterValue) {
    String userId = message.getUserId();
    String userName = message.getUserName();
    User user = getOrCreateUser(userId, userName);

    if (user == null)
      throw new IllegalStateException("Missed user record: " + userId + "-TELEGRAM");

    UserDayStatistics statistics = cache.getUserStatistics(chat, user);

    if (statistics == null) {
      cache.createUserStatistics(chat, user, counterType, counterValue);
    } else {
      cache.updateStatistics(statistics, counterType, counterValue);
    }
  }

  protected User getOrCreateUser(String userId, String userName) {
    try {
      User user = userDAO.find(userId, ChannelType.TELEGRAM);

      if (user == null)
        user = userDAO.create(userId, userName, ChannelType.TELEGRAM);

      return user;
    } catch (ConstraintViolationException ex) {
      if (logger.isDebugEnabled())
        logger.debug("[" + this.getClass().getSimpleName() + "] Duplicate user entry create: " + userId + "-TELEGRAM");

      return userDAO.find(userId, ChannelType.TELEGRAM);
    }
  }
}
