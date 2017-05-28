package com.topicsbot.services.api.telegram;

import com.topicsbot.model.ChannelType;
import com.topicsbot.model.chat.*;
import com.topicsbot.model.chat.ChatType;
import com.topicsbot.services.analysis.AnalysisService;
import com.topicsbot.services.api.telegram.handlers.UpdateHandler;
import com.topicsbot.services.api.telegram.handlers.UpdateProcessor;
import com.topicsbot.services.api.telegram.handlers.UpdateType;
import com.topicsbot.services.api.telegram.handlers.user.*;
import com.topicsbot.services.api.telegram.model.*;
import com.topicsbot.services.api.telegram.model.Chat;
import com.topicsbot.services.cache.CacheService;
import com.topicsbot.services.db.DBService;
import com.topicsbot.services.db.dao.ChatDAO;
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
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Author: Artem Voronov
 */
public class TelegramApiService implements TelegramApiProvider {

  private static final Logger logger = Logger.getLogger("TELEGRAM_API_SERVICE");
  private final TelegramApiClient client;
  private final ScheduledExecutorService scheduledExecutorService;
  private final String sendMessageUrl;
  private final String getChatMembersCountUrl;

  private Queue<Runnable> sendMessageRequestsQueue = new ConcurrentLinkedQueue<>();
  private Queue<Update> updatesQueue = new ConcurrentLinkedQueue<>();

  public TelegramApiService(DBService dbService, ScheduledExecutorService scheduledExecutorService,
                            ResourceBundleService resourceBundleService, AnalysisService analysisService,
                            CacheService cacheService,
                            int connectTimeout, int requestTimeout,
                            String botToken, String botUserName) {
    this.client = new TelegramApiClient(connectTimeout, requestTimeout);
    this.scheduledExecutorService = scheduledExecutorService;
    final String apiTelegramUrl = "https://api.telegram.org/bot"+botToken;
    this.sendMessageUrl = apiTelegramUrl+"/sendMessage";
    this.getChatMembersCountUrl = apiTelegramUrl+"/getChatMembersCount";

    scheduledExecutorService.scheduleWithFixedDelay(new GetUpdatesDaemon(botToken), 10000L, 15L, TimeUnit.MILLISECONDS);
    scheduledExecutorService.scheduleWithFixedDelay(new ProcessUpdatesDaemon(cacheService, analysisService, this, dbService, resourceBundleService, botUserName), 10000L, 15L, TimeUnit.MILLISECONDS);
    scheduledExecutorService.scheduleAtFixedRate(new SendMessageDaemon(), 10000L, 34L, TimeUnit.MILLISECONDS);
  }

  @Override
  public void sendMessage(Chat chat, String text) {
    final String jsonParams = "{\"chat_id\":\"" + chat.getId() + "\",\"text\":\"" + text + "\"}";
    sendMessageRequestsQueue.add(() -> client.makeRequest(sendMessageUrl, jsonParams, Message.class));
  }

  @Override
  public void replyToMessage(Chat chat, String text, Message message) {
    final String jsonParams = "{\"chat_id\":\"" + chat.getId() + "\",\"text\":\"" + text + "\",\"reply_to_message_id\":\"" + message.getId() + "\"}";
    sendMessageRequestsQueue.add(() -> client.makeRequest(sendMessageUrl, jsonParams, Message.class));
  }

  @Override
  public int getChatMembersCount(Chat chat) {
    final String jsonParams = "{\"chat_id\":\"" + chat.getId() + "}";
    ChatMembersCount result = client.makeRequest(getChatMembersCountUrl, jsonParams, ChatMembersCount.class);
    return result.getCount();
  }

  private class SendMessageDaemon implements Runnable {
    @Override
    public void run() {
      try {
        Runnable next = sendMessageRequestsQueue.poll();
        if (next != null)
          scheduledExecutorService.submit(next);
      } catch (Exception ex) {
        logger.error(ex.getMessage(), ex);
      }
    }
  }

  private class ProcessUpdatesDaemon implements Runnable {

    private final UpdateProcessor updateProcessor;
    private final ChatDAO chatDAO;
    private final TopicDAO topicDAO;
    private final UserDAO userDAO;

    ProcessUpdatesDaemon(CacheService cacheService, AnalysisService analysisService, TelegramApiService telegramApiService,
                         DBService db, ResourceBundleService resourceBundleService, String botUserName) {
      this.chatDAO = new ChatDAO(db);
      this.topicDAO = new TopicDAO(db);
      this.userDAO = new UserDAO(db);
      Map<UpdateType, UpdateHandler> handlers = new HashMap<>(UpdateType.values().length);
      handlers.put(UpdateType.START, new StartCommandHandler(telegramApiService, resourceBundleService, chatDAO));
      handlers.put(UpdateType.TO_STATISTICS, new ToStatisticsHandler(analysisService, chatDAO));
      handlers.put(UpdateType.TOPICS, new GetTopicsHandler(analysisService, telegramApiService, chatDAO, topicDAO, resourceBundleService));
      handlers.put(UpdateType.ADD, new AddTopicHandler(chatDAO, topicDAO, userDAO, telegramApiService, resourceBundleService, cacheService));
      handlers.put(UpdateType.WORLD_TOPICS, new GetWorldTopicsHandler(analysisService, telegramApiService, chatDAO, resourceBundleService));
      this.updateProcessor = new UpdateProcessor(botUserName, handlers, cacheService);
    }

    @Override
    public void run() {
      try {
        Update next = updatesQueue.poll();

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
        int size = type == ChatType.PRIVATE ? 1 : getChatMembersCount(apiChat);
        ZoneId UTC = TimeZone.getTimeZone("Etc/GMT0").toZoneId();
        chatDAO.create(externalId, apiChat.getTitle(), ChannelType.TELEGRAM, type, ChatLanguage.EN, size, UTC, LocalDate.now(UTC));
      }
    }
  }

  private class GetUpdatesDaemon implements Runnable {
    private final String getUpdatesUrl;
    private Integer lastUpdateId = null;

    GetUpdatesDaemon(String botToken) {
      this.getUpdatesUrl = "https://api.telegram.org/bot"+botToken+"/getUpdates";
    }

    @Override
    public void run() {
      try {
        String jsonParams = lastUpdateId == null ? "{}" : "{\"offset\":" + lastUpdateId + "}";
        Updates updates = client.makeRequest(getUpdatesUrl, jsonParams, Updates.class);
        if (!updates.isEmpty()) {
          lastUpdateId = updates.getLastUpdateId() + 1;
          updatesQueue.addAll(updates.getUpdates());
        }

      } catch (Exception ex) {
        logger.error(ex.getMessage(), ex);
      }
    }
  }
}
