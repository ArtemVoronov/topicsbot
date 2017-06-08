package com.topicsbot.services.api.telegram.handlers.admin;

import com.topicsbot.services.api.telegram.TelegramApiProvider;
import com.topicsbot.services.api.telegram.handlers.UpdateHandler;
import com.topicsbot.services.api.telegram.model.Message;
import com.topicsbot.services.api.telegram.model.Update;
import com.topicsbot.services.cache.CacheService;

/**
 * Author: Artem Voronov
 */
public class GetJVMInfoHandler extends CommonAdminHandler implements UpdateHandler {

  public GetJVMInfoHandler(TelegramApiProvider telegramApiProvider, CacheService cacheService) {
    super(telegramApiProvider, cacheService);
  }

  @Override
  public void handle(Update update) {
    Message message = update.getMessage();

    if (message == null)
      return;

    if (!isFromAdmin(message))
      return;

    float heapSize = Runtime.getRuntime().totalMemory() / 1048576;
    float heapMaxSize = Runtime.getRuntime().maxMemory() / 1048576;
    float heapFreeSize = Runtime.getRuntime().freeMemory() / 1048576;

    String result = "Current size of heap: " + String.format("%.2f mb", heapSize) + "\n" +
        "Maximum size of heap: " + String.format("%.2f mb", heapMaxSize) + "\n" +
        "Free memory within the heap: " + String.format("%.2f mb", heapFreeSize) + "\n" +
        "OS: " + System.getProperty("os.name") + " " + System.getProperty("os.version") + "\n" +
        "java version: " + System.getProperty("java.version");

    telegramApiProvider.sendMessage(message.getChat(), result);
  }
}
