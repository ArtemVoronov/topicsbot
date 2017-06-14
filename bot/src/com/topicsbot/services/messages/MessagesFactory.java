package com.topicsbot.services.messages;

import com.topicsbot.model.TimeZones;
import com.topicsbot.model.chat.Chat;
import com.topicsbot.model.chat.ChatLanguage;
import com.topicsbot.model.statistics.ChatDayStatistics;
import com.topicsbot.model.statistics.UserDayStatistics;
import com.topicsbot.services.analysis.AnalysisProvider;
import com.topicsbot.services.cache.CacheService;
import com.topicsbot.services.i18n.ResourceBundleService;
import com.topicsbot.utils.TCache;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Author: Artem Voronov
 */
public class MessagesFactory {

  private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
  private static final String STATISTICS_PATH = "http://www.topicsbot.com/chat/";
  private final ResourceBundleService resourceBundleService;
  private final AnalysisProvider analysisProvider;
  private final CacheService cacheService;

  private final TCache<String, Set<String>> cachedWorldTopics = new TCache<>(3L*60*1000); //3 min

  public MessagesFactory(ResourceBundleService resourceBundleService, AnalysisProvider analysisProvider, CacheService cacheService) {
    this.resourceBundleService = resourceBundleService;
    this.analysisProvider = analysisProvider;
    this.cacheService = cacheService;
  }

  public String getWorldTopicsMessage(ChatLanguage language) {
    String dateIsoFormatted = LocalDate.now().toString();
    String languageShort = language.name().toLowerCase();
    List<String> worldKeywords = analysisProvider.getWorldKeywords(dateIsoFormatted, language);
    Set<String> worldTopics = getWorldTopicsCached(worldKeywords, dateIsoFormatted, language);
    List<String> worldHashTags = analysisProvider.getWorldHashTags(dateIsoFormatted, language);

    if ((worldTopics == null || worldTopics.isEmpty()) && (worldHashTags == null || worldHashTags.isEmpty()))
      return resourceBundleService.getMessage(languageShort, "no.world.topics.message");


    int count = 0;
    StringBuilder sb = new StringBuilder();

    if (worldTopics != null && !worldTopics.isEmpty()) {
      sb.append(resourceBundleService.getMessage(languageShort, "topics.header4.message"));
      for (String t : worldTopics) {
        sb.append(++count).append(". ").append(t).append("\n");
      }

      if (worldKeywords != null && !worldKeywords.isEmpty()) {
        String worldKeywordStatisticsMessage = String.join(", ", worldKeywords);
        sb.append(resourceBundleService.getMessage(languageShort, "top.keywords"))
            .append(worldKeywordStatisticsMessage)
            .append("\n");
      }
    }

    if (worldHashTags != null && !worldHashTags.isEmpty()) {
      String worldHashTagsMessage = String.join(", ", worldHashTags);
      sb.append(resourceBundleService.getMessage(languageShort, "popular.hashtags"))
          .append(worldHashTagsMessage);
    }

    return sb.toString();
  }

  private Set<String> getWorldTopicsCached(List<String> worldKeywords, String dateIsoFormatted, ChatLanguage language) {
    String key = dateIsoFormatted + "_" + language;
    Set<String> result = cachedWorldTopics.get(key);

    if (result == null) {
      result = analysisProvider.getWorldTopics(worldKeywords, language);

      if (result != null && !result.isEmpty())
        cachedWorldTopics.putIfNotSet(key, result);
    }

    return result;
  }

  public String getStatisticsMessage(Chat chat, boolean extended) {
    ChatDayStatistics chatStatistics = cacheService.getChatStatistics(chat);
    StringBuilder sb = new StringBuilder(STATISTICS_PATH+chat.getExternalId() + "\n\n" + resourceBundleService.getMessage(chat.getLanguageShort(), "statistics.header.message"));
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
    Map<String, UserDayStatistics> flooders = cacheService.getFlooders(chat);

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

  public String getChatSettingsMessage(Chat chat) {
    String template = resourceBundleService.getMessage(chat.getLanguageShort(), "settings.message");
    String language = chat.getLanguage().getName();
    String prettyTimezone = TimeZones.mappingFrom.get(chat.getTimezone().toString());
    String currentTime = LocalTime.now(chat.getTimezone()).format(TIME_FORMATTER);
    return String.format(template, language, prettyTimezone, currentTime);
  }
}
