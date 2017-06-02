package com.topicsbot.services.api.telegram.handlers.user;

import com.topicsbot.model.chat.Chat;
import com.topicsbot.model.statistics.ChatDayStatistics;
import com.topicsbot.model.statistics.UserDayStatistics;
import com.topicsbot.services.analysis.AnalysisProvider;
import com.topicsbot.services.api.telegram.TelegramApiProvider;
import com.topicsbot.services.api.telegram.handlers.UpdateHandler;
import com.topicsbot.services.api.telegram.model.Message;
import com.topicsbot.services.api.telegram.model.Update;
import com.topicsbot.services.cache.CacheService;
import com.topicsbot.services.db.dao.ChatDAO;
import com.topicsbot.services.i18n.ResourceBundleService;

import java.util.List;
import java.util.Map;

/**
 * Author: Artem Voronov
 */
public class GetStatisticsHandler implements UpdateHandler {
  private final AnalysisProvider analysisProvider;
  private final TelegramApiProvider telegramApiProvider;
  private final ChatDAO chatDAO;
  private final CacheService cache;
  private final ResourceBundleService resourceBundleService;

  public GetStatisticsHandler(AnalysisProvider analysisProvider, TelegramApiProvider telegramApiProvider,
                              ChatDAO chatDAO, CacheService cache,
                              ResourceBundleService resourceBundleService) {
    this.analysisProvider = analysisProvider;
    this.telegramApiProvider = telegramApiProvider;
    this.chatDAO = chatDAO;
    this.cache = cache;
    this.resourceBundleService = resourceBundleService;
  }

  @Override
  public void handle(Update update) {
    Message message = update.getMessage();

    if (message == null)
      return;

    Chat chat = chatDAO.find(message.getChatId());
    String result = getStatisticsMessage(chat, false);
    telegramApiProvider.sendMessage(message.getChat(), result);
  }

  private String getStatisticsMessage(Chat chat, boolean extended) {
    ChatDayStatistics chatStatistics = cache.getChatStatistics(chat);
    StringBuilder sb = new StringBuilder(resourceBundleService.getMessage(chat.getLanguageShort(), "statistics.header.message"));

    if (chatStatistics == null) {
      sb.append(resourceBundleService.getMessage(chat.getLanguageShort(), "messages.count"))
          .append(0)
          .append(resourceBundleService.getMessage(chat.getLanguageShort(), "words.count"))
          .append(0)
          .append(resourceBundleService.getMessage(chat.getLanguageShort(), "flood.size.count"))
          .append(0);
      return sb.toString();
    } else {
      sb.append(resourceBundleService.getMessage(chat.getLanguageShort(), "messages.count"))
          .append(chatStatistics.getMessageCounter())
          .append(resourceBundleService.getMessage(chat.getLanguageShort(), "words.count"))
          .append(chatStatistics.getWordCounter())
          .append(resourceBundleService.getMessage(chat.getLanguageShort(), "flood.size.count"))
          .append(chatStatistics.getFloodSize())
          .append("\n");

      String userStatistics = getUserStatisticsMessage(chat, chatStatistics.getFloodSize(), extended);

      if (!"".equals(userStatistics)) {
        sb.append("\n").append(userStatistics);
      }

      List<String> keywords = analysisProvider.getChatKeywords(chat);

      if (keywords != null && !keywords.isEmpty()) {
        String keywordStatistics = String.join(", ", keywords);
        sb.append(resourceBundleService.getMessage(chat.getLanguageShort(), "top.keywords")).append(keywordStatistics);
      }

      return sb.toString();
    }
  }

  private String getUserStatisticsMessage(Chat chat, int chatFloodSize, boolean extended) {
    if (chatFloodSize == 0)
      return "";

    StringBuilder sb = new StringBuilder();
    Map<String, UserDayStatistics> flooders = cache.getFlooders(chat);

    if (flooders == null) {
      return "";
    }

    flooders.values().stream()
        .sorted((o1, o2) -> Long.compare(o2.getFloodSize(), o1.getFloodSize()))
        .limit(10)
        .forEach(stat -> {

          float percentFlood = (float) stat.getFloodSize() / chatFloodSize * 100;

          if (Float.isNaN(percentFlood)) {
            percentFlood = 0;
          }

          if (percentFlood != 0) {
            sb.append(stat.getUser().getName()).append(": ");
            sb.append(String.format("%.2f", percentFlood)).append(" %");

            if (extended) {
              sb.append(" (").append(stat.getFloodSize()).append(")");
            }

            sb.append("\n");
          }
        });


    if (sb.length() != 0) {
      sb.insert(0, resourceBundleService.getMessage(chat.getLanguageShort(), "flooders.olymp.header"));
    }

    return sb.toString();
  }

}
