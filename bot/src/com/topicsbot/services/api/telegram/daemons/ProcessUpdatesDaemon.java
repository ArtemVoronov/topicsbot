package com.topicsbot.services.api.telegram.daemons;

import com.topicsbot.model.ChannelType;
import com.topicsbot.model.chat.ChatLanguage;
import com.topicsbot.model.chat.ChatType;
import com.topicsbot.services.analysis.AnalysisService;
import com.topicsbot.services.api.telegram.TelegramApiProvider;
import com.topicsbot.services.api.telegram.handlers.UpdateHandler;
import com.topicsbot.services.api.telegram.handlers.UpdateProcessor;
import com.topicsbot.services.api.telegram.handlers.UpdateType;
import com.topicsbot.services.api.telegram.handlers.user.*;
import com.topicsbot.services.api.telegram.model.Chat;
import com.topicsbot.services.api.telegram.model.Converter;
import com.topicsbot.services.api.telegram.model.Message;
import com.topicsbot.services.api.telegram.model.Update;
import com.topicsbot.services.cache.CacheService;
import com.topicsbot.services.db.DBService;
import com.topicsbot.services.db.dao.ChatDAO;
import com.topicsbot.services.db.dao.StatisticsDAO;
import com.topicsbot.services.db.dao.TopicDAO;
import com.topicsbot.services.db.dao.UserDAO;
import com.topicsbot.services.i18n.ResourceBundleService;
import org.apache.log4j.Logger;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.TimeZone;

/**
 * Author: Artem Voronov
 */
public class ProcessUpdatesDaemon implements Runnable {
  private static final Logger logger = Logger.getLogger("PROCESS_UPDATES_DAEMON");

  private final Queue<Update> updates;
  private final TelegramApiProvider telegramApiProvider;

  private final UpdateProcessor updateProcessor;
  private final ChatDAO chatDAO;

  public ProcessUpdatesDaemon(TelegramApiProvider telegramApiProvider, Queue<Update> updates,
                       CacheService cacheService, AnalysisService analysisService ,
                       DBService db, ResourceBundleService resourceBundleService,
                       String botUserName) {

    this.updates = updates;
    this.telegramApiProvider = telegramApiProvider;
    this.chatDAO = new ChatDAO(db);
    final TopicDAO topicDAO = new TopicDAO(db);
    final UserDAO userDAO = new UserDAO(db);
    Map<UpdateType, UpdateHandler> handlers = new HashMap<>(UpdateType.values().length);
    handlers.put(UpdateType.START, new StartCommandHandler(telegramApiProvider, resourceBundleService, chatDAO));
    handlers.put(UpdateType.TO_STATISTICS, new ToStatisticsHandler(analysisService, cacheService, chatDAO, userDAO));
    handlers.put(UpdateType.TOPICS, new GetTopicsHandler(analysisService, telegramApiProvider, chatDAO, topicDAO, resourceBundleService));
    handlers.put(UpdateType.ADD, new AddTopicHandler(chatDAO, topicDAO, userDAO, telegramApiProvider, resourceBundleService, cacheService));
    handlers.put(UpdateType.WORLD_TOPICS, new GetWorldTopicsHandler(analysisService, telegramApiProvider, chatDAO, resourceBundleService));
    handlers.put(UpdateType.STATISTICS, new GetStatisticsHandler(analysisService, telegramApiProvider, chatDAO, cacheService, resourceBundleService));
    this.updateProcessor = new UpdateProcessor(botUserName, handlers, cacheService);
  }

  @Override
  public void run() {
    try {
      Update next = updates.poll();

      if (next == null)
        return;

      if (isDeprecated(next))
        return;

      checkOrRegisterChat(next);
      updateProcessor.process(next);

    } catch (Exception ex) {
      logger.error(ex.getMessage(), ex);
    }
  }

  private boolean isDeprecated(Update update) {
    Message message = update.getMessage();

    if (message == null)
      return false;

    long message_time = message.getDate() * 1000;
    long system_time = System.currentTimeMillis();
    long diff = system_time - message_time;
    return diff > 120_000L; //if older than 2 min -> ignore message
  }

  private void checkOrRegisterChat(Update update) {
    Message message = update.getMessage();

    if (message == null)
      return;

    String externalId = message.getChatId();
    com.topicsbot.model.chat.Chat modelChat = chatDAO.find(externalId);

    if (modelChat == null) {
      Chat apiChat = message.getChat();
      com.topicsbot.model.chat.ChatType type = Converter.convert(apiChat.getType());
      int size = type == ChatType.PRIVATE ? 1 : telegramApiProvider.getChatMembersCount(apiChat);
      ZoneId UTC = TimeZone.getTimeZone("Etc/GMT0").toZoneId();
      chatDAO.create(externalId, apiChat.getTitle(), ChannelType.TELEGRAM, type, ChatLanguage.EN, size, UTC, LocalDate.now(UTC));
    }
  }
}