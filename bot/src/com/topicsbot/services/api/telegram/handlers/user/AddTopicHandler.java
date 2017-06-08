package com.topicsbot.services.api.telegram.handlers.user;

import com.topicsbot.model.ChannelType;
import com.topicsbot.model.chat.Chat;
import com.topicsbot.model.user.User;
import com.topicsbot.services.api.telegram.TelegramApiProvider;
import com.topicsbot.services.api.telegram.handlers.UpdateHandler;
import com.topicsbot.services.api.telegram.handlers.UpdateType;
import com.topicsbot.services.api.telegram.model.Message;
import com.topicsbot.services.api.telegram.model.Update;
import com.topicsbot.services.cache.CacheService;
import com.topicsbot.services.db.dao.ChatDAO;
import com.topicsbot.services.db.dao.TopicDAO;
import com.topicsbot.services.db.dao.UserDAO;
import com.topicsbot.services.i18n.ResourceBundleService;
import org.apache.log4j.Logger;
import org.hibernate.exception.ConstraintViolationException;

/**
 * Author: Artem Voronov
 */
public class AddTopicHandler implements UpdateHandler {
  private static final Logger logger = Logger.getLogger("PROCESS_UPDATES_DAEMON");
  private static final int TOPIC_MAX_LENGTH = 255;
  private static final int TOPICS_LIMIT = 7;

  private final ChatDAO chatDAO;
  private final TopicDAO topicDAO;
  private final UserDAO userDAO;
  private final TelegramApiProvider telegramApiProvider;
  private final ResourceBundleService resourceBundleService;
  private final CacheService cacheService;
  private final String botUserName;


  public AddTopicHandler(String botUserName, ChatDAO chatDAO, TopicDAO topicDAO, UserDAO userDAO,
                         TelegramApiProvider telegramApiProvider, ResourceBundleService resourceBundleService,
                         CacheService cacheService) {
    this.botUserName = botUserName;
    this.chatDAO = chatDAO;
    this.topicDAO = topicDAO;
    this.userDAO = userDAO;
    this.telegramApiProvider = telegramApiProvider;
    this.resourceBundleService = resourceBundleService;
    this.cacheService = cacheService;
  }

  @Override
  public void handle(Update update) {
    Message message = update.getMessage();

    if (message == null)
      return;

    String text = message.getText();
    String topic = message.getText();

    boolean startsWithForwardSlash = text.startsWith("/");

    if (!startsWithForwardSlash) {
      topic = text;
      cacheService.removeWaiter(message.getChatId(), message.getUserId());
    }

    if (startsWithForwardSlash) {
      String[] parameters = text.replace(botUserName, "").split(UpdateType.ADD.getCommand());
      topic = parameters.length > 1 ? parameters[1].trim() : null;
    }

    Chat chat = chatDAO.find(message.getChatId());

    if (topic == null) {
      cacheService.addWaiter(message.getChatId(), message.getUserId());
      String feedback = resourceBundleService.getMessage(chat.getLanguageShort(), "add.topics.question.message");
      telegramApiProvider.sendMessage(message.getChat(), feedback);
      return;
    }

    if (topic.isEmpty()) {
      String feedback = resourceBundleService.getMessage(chat.getLanguageShort(), "topic.missed.message");
      telegramApiProvider.sendMessage(message.getChat(), feedback);
      return;
    }

    if (topic.length() > TOPIC_MAX_LENGTH) {
      String feedback = resourceBundleService.getMessage(chat.getLanguageShort(), "too.long.topics.message");
      telegramApiProvider.sendMessage(message.getChat(), feedback);
      return;
    }

    if (isTooManyTopicsToday(chat)) {
      String feedback = resourceBundleService.getMessage(chat.getLanguageShort(), "add.topic.limit.message");
      telegramApiProvider.sendMessage(message.getChat(), feedback);
      return;
    }

    String userId = message.getUserId();
    String userName = message.getUserName();
    User user = getOrCreateUser(userId, userName);

    if (user == null) {
      String feedback = resourceBundleService.getMessage(chat.getLanguageShort(), "error.message");
      telegramApiProvider.replyToMessage(message.getChat(), feedback, message);
      throw new IllegalStateException("Missed user record: " + userId + "-TELEGRAM");
    }

    topicDAO.create(chat, topic, user, chat.getRebirthDate());

    String feedback = resourceBundleService.getMessage(chat.getLanguageShort(), "add.topic.success.message");
    telegramApiProvider.replyToMessage(message.getChat(), feedback, message);
  }

  private User getOrCreateUser(String userId, String userName) {//TODO: duplicate operation
    try {
      User user = userDAO.find(userId, ChannelType.TELEGRAM);

      if (user == null)
        user = userDAO.create(userId, userName, ChannelType.TELEGRAM);

      return user;
    } catch (ConstraintViolationException ex) {
      if (logger.isDebugEnabled())
        logger.debug("[ADD_TOPIC] Duplicate user entry create: " + userId + "-TELEGRAM");

      return userDAO.find(userId, ChannelType.TELEGRAM);
    }
  }

  private boolean isTooManyTopicsToday(Chat chat) {
    return getHumanTopicsCount(chat) >= TOPICS_LIMIT;
  }

  private int getHumanTopicsCount(Chat chat) {
    return topicDAO.find(chat, chat.getRebirthDate()).size();
  }
}
