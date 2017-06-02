package com.topicsbot.services.api.telegram;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.topicsbot.services.analysis.AnalysisService;
import com.topicsbot.services.api.telegram.daemons.GetUpdatesDaemon;
import com.topicsbot.services.api.telegram.daemons.ProcessUpdatesDaemon;
import com.topicsbot.services.api.telegram.daemons.RebirthChatDaemon;
import com.topicsbot.services.api.telegram.daemons.SendMessageDaemon;
import com.topicsbot.services.api.telegram.model.*;
import com.topicsbot.services.cache.CacheService;
import com.topicsbot.services.db.DBService;
import com.topicsbot.services.i18n.ResourceBundleService;
import org.apache.log4j.Logger;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Author: Artem Voronov
 */
public class TelegramApiService implements TelegramApiProvider {
  private static final Logger logger = Logger.getLogger("TELEGRAM_API_SERVICE");
  private static final ObjectMapper MAPPER = new ObjectMapper();
  static {
    MAPPER.registerModule(new JavaTimeModule());
    MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  }

  private final TelegramApiClient client;
  private final String getUpdatesUrl;
  private final String sendMessageUrl;
  private final String getChatMembersCountUrl;

  private final Queue<Runnable> sendMessageRequestsQueue = new ConcurrentLinkedQueue<>();
  private final Queue<Update> updatesQueue = new ConcurrentLinkedQueue<>();

  public TelegramApiService(DBService dbService, ScheduledExecutorService scheduledExecutorService,
                            ResourceBundleService resourceBundleService, AnalysisService analysisService,
                            CacheService cacheService,
                            int connectTimeout, int requestTimeout,
                            String botToken, String botUserName) {
    this.client = new TelegramApiClient(connectTimeout, requestTimeout);

    final String apiTelegramUrl = "https://api.telegram.org/bot"+botToken;
    this.getUpdatesUrl = apiTelegramUrl + "/getUpdates";
    this.sendMessageUrl = apiTelegramUrl + "/sendMessage";
    this.getChatMembersCountUrl = apiTelegramUrl + "/getChatMembersCount";

    scheduledExecutorService.scheduleWithFixedDelay(new GetUpdatesDaemon(updatesQueue, this), 10000L, 15L, TimeUnit.MILLISECONDS);
    scheduledExecutorService.scheduleWithFixedDelay(new ProcessUpdatesDaemon(this, updatesQueue, cacheService, analysisService, dbService, resourceBundleService, botUserName), 10000L, 15L, TimeUnit.MILLISECONDS);
    scheduledExecutorService.scheduleAtFixedRate(new SendMessageDaemon(sendMessageRequestsQueue, scheduledExecutorService), 10000L, 34L, TimeUnit.MILLISECONDS);
    scheduledExecutorService.scheduleWithFixedDelay(new RebirthChatDaemon(dbService), 15L, 3600L, TimeUnit.SECONDS);
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

  @Override
  public Updates getUpdates(Integer lastUpdateId) {
    String jsonParams = lastUpdateId == null ? "{}" : "{\"offset\":" + lastUpdateId + "}";
    return client.makeRequest(getUpdatesUrl, jsonParams, Updates.class);
  }

  @Override
  public void sendReplyKeyboard(Chat chat, String text, ReplyKeyboardMarkup keyboard) {
    try {
      final String jsonParams = "{\"chat_id\":\"" + chat.getId() + "\",\"text\":\"" + text + "\",\"reply_markup\":" + MAPPER.writeValueAsString(keyboard) + "}";
      sendMessageRequestsQueue.add(() -> client.makeRequest(sendMessageUrl, jsonParams, Message.class));
    } catch (Exception ex) {
      logger.error(ex.getMessage(), ex);
    }
  }

  @Override
  public void hideKeyboard(Chat chat, String text, ReplyKeyboardRemove replyKeyboardRemove) {
    try {
      final String jsonParams = "{\"chat_id\":\"" + chat.getId() + "\",\"text\":\"" + text + "\",\"reply_markup\":" + MAPPER.writeValueAsString(replyKeyboardRemove) + "}";
      sendMessageRequestsQueue.add(() -> client.makeRequest(sendMessageUrl, jsonParams, Message.class));
    } catch (Exception ex) {
      logger.error(ex.getMessage(), ex);
    }
  }

  @Override
  public void sendInlineKeyboard() {

  }
}
