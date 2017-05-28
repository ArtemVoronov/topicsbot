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

/**
 * Author: Artem Voronov
 */
public class AddTopicHandler implements UpdateHandler {
  public static final int TOPIC_MAX_LENGTH = 255;

  private final ChatDAO chatDAO;
  private final TopicDAO topicDAO;
  private final UserDAO userDAO;
  private final TelegramApiProvider telegramApiProvider;
  private final ResourceBundleService resourceBundleService;
  private final CacheService cacheService;


  public AddTopicHandler(ChatDAO chatDAO, TopicDAO topicDAO, UserDAO userDAO,
                         TelegramApiProvider telegramApiProvider, ResourceBundleService resourceBundleService,
                         CacheService cacheService) {
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
      String[] parameters = text.split(UpdateType.ADD.getCommand());
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

    String userId = message.getUserId();
    String userName = message.getUserName();
    User user = getOrCreateUser(userId, userName);
    topicDAO.create(chat, topic, user, chat.getRebirthDate());

    String feedback = resourceBundleService.getMessage(chat.getLanguageShort(), "add.topic.success.message");
    telegramApiProvider.sendMessage(message.getChat(), feedback);
  }

  private User getOrCreateUser(String userId, String userName) {
    User user = userDAO.find(userId, ChannelType.TELEGRAM);

    if (user == null) {
      user = userDAO.create(userId, userName, ChannelType.TELEGRAM);
    }

    return user;
  }

  //TODO: clean
//  public static void main(String[] args) {
//    LocalDateTime ldt1 = LocalDateTime.now(TimeZone.getTimeZone(TimeZones.GMT_PLUS_12.getName()).toZoneId());
//    LocalDateTime ldt2 = LocalDateTime.now(TimeZone.getTimeZone(TimeZones.GMT_MINUS_13.getName()).toZoneId());
//
//
//    LocalDate ld1 = LocalDate.now(TimeZone.getTimeZone(TimeZones.GMT_PLUS_12.getName()).toZoneId());
//    LocalDate ld2 = LocalDate.now(TimeZone.getTimeZone(TimeZones.GMT_MINUS_13.getName()).toZoneId());
//
//    String s1= ldt1.format(DateTimeFormatter.ISO_LOCAL_DATE);
//    String s2= ldt2.format(DateTimeFormatter.ISO_LOCAL_DATE);
//    System.out.println(s1);
//    System.out.println(s2);
//
//
//    System.out.println(ld1);
//    System.out.println(ld2);
//  }
}
