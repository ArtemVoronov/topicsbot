package com.topicsbot.services.api.telegram.handlers.admin;

import com.topicsbot.model.chat.ChatType;
import com.topicsbot.services.api.telegram.TelegramApiProvider;
import com.topicsbot.services.api.telegram.handlers.UpdateHandler;
import com.topicsbot.services.api.telegram.model.Message;
import com.topicsbot.services.api.telegram.model.Update;
import com.topicsbot.services.cache.CacheInfo;
import com.topicsbot.services.cache.CacheService;

import java.time.LocalDate;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Author: Artem Voronov
 */
public class CountHandler extends CommonAdminHandler implements UpdateHandler {

  public CountHandler(TelegramApiProvider telegramApiProvider, CacheService cacheService) {
    super(telegramApiProvider, cacheService);
  }

  @Override
  public void handle(Update update) {
    Message message = update.getMessage();

    if (message == null)
      return;

    if (!isFromAdmin(message))
      return;

    CacheInfo cacheInfo = cache.getCacheInfo(LocalDate.now());
    StringBuilder sb = new StringBuilder();

    sb.append("chats: ").append(cacheInfo.getActiveChats()).append("\n");
    sb.append("users: ").append(cacheInfo.getActiveUsers()).append("\n");
//    sb.append("total counter:\n").append(toPrettyMap(cacheInfo.getChatCounters())).append("\n");
//    sb.append("total languages:\n").append(toPrettyMap(cacheInfo.getChatLanguages())).append("\n");
//    sb.append("total timezones:\n").append(toPrettyMap(cacheInfo.getChatTimeZones())).append("\n");
//    sb.append("detailed counter:\n").append(toPrettyMap2(cacheInfo.getChatCountersDetailed())).append("\n");
//    sb.append("detailed languages:\n").append(toPrettyMap2(cacheInfo.getChatLanguagesDetailed())).append("\n");
//    sb.append("detailed timezones:\n").append(toPrettyMap2(cacheInfo.getChatTimeZonesDetailed())).append("\n");

    //TODO: add servlet or web page for cache info

    String result = sb.toString();
    telegramApiProvider.sendMessage(message.getChat(), result);
  }

  private static String toPrettyMap(Map<?, ?> map) {
    return map.entrySet().stream().map(e -> "" + e.getKey() + ": " +e.getValue()).collect(Collectors.joining("\n"));
  }

  private static String toPrettyMap2(Map<?, Map<ChatType, Integer>> map) {
    return map.entrySet().stream().map(e -> "" + e.getKey() + ": " +toPrettyMap(e.getValue())).collect(Collectors.joining("\n"));
  }
}
