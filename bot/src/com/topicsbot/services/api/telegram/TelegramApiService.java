package com.topicsbot.services.api.telegram;

import com.topicsbot.services.api.telegram.model.Update;
import com.topicsbot.services.api.telegram.model.Updates;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Author: Artem Voronov
 */
public class TelegramApiService implements TelegramApiProvider {

  private final TelegramApiClient client;
  private final ScheduledExecutorService scheduledExecutorService;

  private Queue<Runnable> sendMessageRequestsQueue = new ConcurrentLinkedQueue<>();
  private Queue<Update> updatesQueue = new ConcurrentLinkedQueue<>();

  public TelegramApiService(ScheduledExecutorService scheduledExecutorService, int connectTimeout, int requestTimeout, String botToken) {
    this.client = new TelegramApiClient(connectTimeout, requestTimeout);
    this.scheduledExecutorService = scheduledExecutorService;

    scheduledExecutorService.scheduleWithFixedDelay(new GetUpdatesDaemon(botToken), 10000L, 15L, TimeUnit.MILLISECONDS);
    scheduledExecutorService.scheduleWithFixedDelay(new ProcessUpdatesDaemon(), 10000L, 15L, TimeUnit.MILLISECONDS);
    scheduledExecutorService.scheduleAtFixedRate(new SendMessageDaemon(), 10000L, 34L, TimeUnit.MILLISECONDS);
  }

  private class SendMessageDaemon implements Runnable {
    @Override
    public void run() {
      Runnable next = sendMessageRequestsQueue.poll();
      if (next != null)
        scheduledExecutorService.submit(next);
    }
  }

  private class ProcessUpdatesDaemon implements Runnable {
    @Override
    public void run() {
      Update next = updatesQueue.poll();
      if (next != null)
        System.out.println(next.getMessage().getText());//TODO
    }
  }

  private class GetUpdatesDaemon implements Runnable {
    private final String getUpdatesUrl;
    private Integer lastUpdateId = null;

    GetUpdatesDaemon(String botToken) {
      this.getUpdatesUrl = "https://api.telegram.org/bot"+botToken+"/getUpdates";;
    }

    @Override
    public void run() {
      String jsonParams = lastUpdateId == null ? "{}" : "{\"offset\":" + lastUpdateId + "}";
      Updates updates = client.makeRequest(getUpdatesUrl, jsonParams, Updates.class);
      if (!updates.isEmpty()) {
        lastUpdateId = updates.getLastUpdateId() + 1;
        updatesQueue.addAll(updates.getUpdates());
      }
    }
  }
}
