package com.topicsbot.services.api.telegram.handlers.user;

import com.topicsbot.model.chat.Chat;
import com.topicsbot.model.statistics.CounterType;
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

/**
 * Author: Artem Voronov
 */
public class AddTopicUserHandler extends CommonUserHandler implements UpdateHandler {
  private static final int TOPIC_MAX_LENGTH = 255;
  private static final int TOPICS_LIMIT = 7;

  private final ChatDAO chatDAO;
  private final TopicDAO topicDAO;
  private final TelegramApiProvider telegramApiProvider;
  private final ResourceBundleService resourceBundleService;
  private final String botUserName;


  public AddTopicUserHandler(String botUserName, ChatDAO chatDAO, TopicDAO topicDAO, UserDAO userDAO,
                             TelegramApiProvider telegramApiProvider, ResourceBundleService resourceBundleService,
                             CacheService cache) {
    super(cache, userDAO);
    this.botUserName = botUserName;
    this.chatDAO = chatDAO;
    this.topicDAO = topicDAO;
    this.telegramApiProvider = telegramApiProvider;
    this.resourceBundleService = resourceBundleService;
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
      cache.removeWaiter(message.getChatId(), message.getUserId());
    }

    if (startsWithForwardSlash) {
      String[] parameters = text.replace(botUserName, "").split(UpdateType.ADD.getCommand());
      topic = parameters.length > 1 ? parameters[1].trim() : null;
    }

    Chat chat = chatDAO.find(message.getChatId());

    if (topic == null) {
      cache.addWaiter(message.getChatId(), message.getUserId());
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

    updateChatCounters(chat, CounterType.ADD_TOPIC_COMMAND, 1);
    updateUserCounter(message, chat, CounterType.ADD_TOPIC_COMMAND, 1);
  }

  private boolean isTooManyTopicsToday(Chat chat) {
    return getHumanTopicsCount(chat) >= TOPICS_LIMIT;
  }

  private int getHumanTopicsCount(Chat chat) {
    return topicDAO.find(chat, chat.getRebirthDate()).size();
  }
}
