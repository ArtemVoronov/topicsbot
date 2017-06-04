package com.topicsbot.services.api.telegram.handlers.user;

import com.topicsbot.model.chat.Chat;
import com.topicsbot.model.chat.ChatLanguage;
import com.topicsbot.services.analysis.AnalysisService;
import com.topicsbot.services.api.telegram.TelegramApiProvider;
import com.topicsbot.services.api.telegram.handlers.UpdateHandler;
import com.topicsbot.services.api.telegram.model.*;
import com.topicsbot.services.db.dao.ChatDAO;
import com.topicsbot.services.i18n.ResourceBundleService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Author: Artem Voronov
 */
public class AnswerInlineQueryHandler implements UpdateHandler {
  private final AnalysisService analysisService;
  private final TelegramApiProvider telegramApiProvider;
  private final ChatDAO chatDAO;
  private final ResourceBundleService resourceBundleService;

  public AnswerInlineQueryHandler(AnalysisService analysisService, TelegramApiProvider telegramApiProvider,
                                  ChatDAO chatDAO, ResourceBundleService resourceBundleService) {
    this.analysisService = analysisService;
    this.telegramApiProvider = telegramApiProvider;
    this.chatDAO = chatDAO;
    this.resourceBundleService = resourceBundleService;
  }

  @Override
  public void handle(Update update) {
    InlineQuery inlineQuery = update.getInlineQuery();

    if (inlineQuery == null)
      return;

    List<InlineQueryResult> result = createInlineQueryResults();
    telegramApiProvider.answerInlineQuery(inlineQuery.getId(), result);
  }

  private List<InlineQueryResult> createInlineQueryResults() {
    String worldTopicsMessage = getWorldTopicsMessageDefault();

    InputTextMessageContent worldTopicsResult = new InputTextMessageContent();
    worldTopicsResult.setMessageText(worldTopicsMessage);

    InlineQueryResultArticle worldTopicsArticle = new InlineQueryResultArticle();
    worldTopicsArticle.setId("get_world_topics_en");
    worldTopicsArticle.setTitle("Topics");
    worldTopicsArticle.setDescription("See what has been discussed in Telegram today");
    worldTopicsArticle.setInputMessageContent(worldTopicsResult);


    List<InlineQueryResult> inlineQueryResults = new ArrayList<>();
    inlineQueryResults.add(worldTopicsArticle);

    return inlineQueryResults;
  }

  private String getWorldTopicsMessageDefault() {
    String dateIsoFormatted = LocalDate.now().toString();
    ChatLanguage language = ChatLanguage.EN;
    String languageShort = ChatLanguage.EN.name().toLowerCase();
    List<String> worldKeywords = analysisService.getWorldKeywords(dateIsoFormatted, language);
    Set<String> worldTopics = analysisService.getWorldTopics(worldKeywords, language);
    List<String> worldHashTags = analysisService.getWorldHashTags(dateIsoFormatted, language);

    if ((worldTopics == null || worldTopics.isEmpty()) && (worldHashTags == null || worldHashTags.isEmpty()))
      return resourceBundleService.getMessage(languageShort, "no.topics.message");


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
}