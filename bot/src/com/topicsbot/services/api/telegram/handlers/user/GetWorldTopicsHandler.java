package com.topicsbot.services.api.telegram.handlers.user;

import com.topicsbot.model.chat.Chat;
import com.topicsbot.model.chat.ChatLanguage;
import com.topicsbot.services.analysis.AnalysisService;
import com.topicsbot.services.api.telegram.TelegramApiProvider;
import com.topicsbot.services.api.telegram.handlers.UpdateHandler;
import com.topicsbot.services.api.telegram.model.Message;
import com.topicsbot.services.api.telegram.model.Update;
import com.topicsbot.services.db.dao.ChatDAO;
import com.topicsbot.services.i18n.ResourceBundleService;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * Author: Artem Voronov
 */
public class GetWorldTopicsHandler implements UpdateHandler {
  private final AnalysisService analysisService;
  private final TelegramApiProvider telegramApiProvider;
  private final ChatDAO chatDAO;
  private final ResourceBundleService resourceBundleService;

  public GetWorldTopicsHandler(AnalysisService analysisService, TelegramApiProvider telegramApiProvider,
                               ChatDAO chatDAO, ResourceBundleService resourceBundleService) {
    this.analysisService = analysisService;
    this.telegramApiProvider = telegramApiProvider;
    this.chatDAO = chatDAO;
    this.resourceBundleService = resourceBundleService;
  }

  @Override
  public void handle(Update update) {
    Message message = update.getMessage();

    if (message == null)
      return;

    Chat chat = chatDAO.find(message.getChatId());
    String result = getWorldTopicsMessage(chat);
    telegramApiProvider.sendMessage(message.getChat(), result);
  }

  private String getWorldTopicsMessage(Chat chat) {
    String dateIsoFormatted = LocalDate.now().toString();
    ChatLanguage language = chat.getLanguage();
    List<String> worldKeywords = analysisService.getWorldKeywords(dateIsoFormatted, language);
    Set<String> worldTopics = analysisService.getWorldTopics(worldKeywords, language);
    List<String> worldHashTags = analysisService.getWorldHashTags(dateIsoFormatted, language);

    if ((worldTopics == null || worldTopics.isEmpty()) && (worldHashTags == null || worldHashTags.isEmpty()))
      return resourceBundleService.getMessage(chat.getLanguageShort(), "no.topics.message");


    int count = 0;
    StringBuilder sb = new StringBuilder();

    if (worldTopics != null && !worldTopics.isEmpty()) {
      sb.append(resourceBundleService.getMessage(chat.getLanguageShort(), "topics.header4.message"));
      for (String t : worldTopics) {
        sb.append(++count).append(". ").append(t).append("\n");
      }

      if (worldKeywords != null && !worldKeywords.isEmpty()) {
        String worldKeywordStatisticsMessage = String.join(", ", worldKeywords);
        sb.append(resourceBundleService.getMessage(chat.getLanguageShort(), "top.keywords"))
            .append(worldKeywordStatisticsMessage)
            .append("\n");
      }
    }

    if (worldHashTags != null && !worldHashTags.isEmpty()) {
      String worldHashTagsMessage = String.join(", ", worldHashTags);
      sb.append(resourceBundleService.getMessage(chat.getLanguageShort(), "popular.hashtags"))
          .append(worldHashTagsMessage);
    }

    return sb.toString();
  }
}