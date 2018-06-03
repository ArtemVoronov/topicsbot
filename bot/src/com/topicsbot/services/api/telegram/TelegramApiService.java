package com.topicsbot.services.api.telegram;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.topicsbot.services.api.telegram.daemons.SendMessageDaemon;
import com.topicsbot.services.api.telegram.daemons.SendMessageToChatDaemon;
import com.topicsbot.services.api.telegram.model.Chat;
import com.topicsbot.services.api.telegram.model.ChatInfo;
import com.topicsbot.services.api.telegram.model.Message;
import com.topicsbot.services.db.DBService;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Author: Artem Voronov
 */
public class TelegramApiService implements TelegramApiProvider {
  private static final ObjectMapper MAPPER = new ObjectMapper();
  static {
    MAPPER.registerModule(new JavaTimeModule());
    MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  }

  private final TelegramApiClient client;
  private final String sendMessageUrl;
  private final String getChatUrl;
  private final Queue<Runnable> sendMessageRequestsQueue = new ConcurrentLinkedQueue<>();

  public TelegramApiService(DBService dbService, ScheduledExecutorService scheduledExecutorService,
                            int connectTimeout, int requestTimeout,
                            String botToken) {
    this.client = new TelegramApiClient(connectTimeout, requestTimeout);

    final String apiTelegramUrl = "https://api.telegram.org/bot"+botToken;
    this.sendMessageUrl = apiTelegramUrl + "/sendMessage";
    this.getChatUrl = apiTelegramUrl + "/getChat";

    scheduledExecutorService.scheduleAtFixedRate(new SendMessageDaemon(sendMessageRequestsQueue, scheduledExecutorService), 10000L, 34L, TimeUnit.MILLISECONDS);
    scheduledExecutorService.schedule(new SendMessageToChatDaemon(dbService, this), 60L, TimeUnit.SECONDS);
  }


  @Override
  public void sendMessage(String chatExternalId, String text) {
    final String jsonParams = "{\"chat_id\":\"" + chatExternalId + "\",\"text\":\"" + text + "\"}";
    sendMessageRequestsQueue.add(() -> client.makeRequest(sendMessageUrl, jsonParams, Message.class));
  }

  @Override
  public Chat getChat(String chatExternalId) {
    final String jsonParams = "{\"chat_id\":\"" + chatExternalId + "\"}";
    ChatInfo result = client.makeRequest(getChatUrl, jsonParams, ChatInfo.class);
    return result.getChat();
  }
}
