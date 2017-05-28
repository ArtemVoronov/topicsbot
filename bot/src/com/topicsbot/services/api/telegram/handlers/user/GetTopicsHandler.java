package com.topicsbot.services.api.telegram.handlers.user;

import com.topicsbot.model.chat.Chat;
import com.topicsbot.model.topic.Topic;
import com.topicsbot.services.analysis.AnalysisService;
import com.topicsbot.services.api.telegram.TelegramApiProvider;
import com.topicsbot.services.api.telegram.handlers.UpdateHandler;
import com.topicsbot.services.api.telegram.model.Message;
import com.topicsbot.services.api.telegram.model.Update;
import com.topicsbot.services.db.dao.ChatDAO;
import com.topicsbot.services.db.dao.TopicDAO;
import com.topicsbot.services.i18n.ResourceBundleService;

import java.util.List;
import java.util.Set;

/**
 * Author: Artem Voronov
 */
public class GetTopicsHandler implements UpdateHandler {
  private final AnalysisService analysisService;
  private final TelegramApiProvider telegramApiProvider;
  private final ChatDAO chatDAO;
  private final TopicDAO topicDAO;
  private final ResourceBundleService resourceBundleService;

  public GetTopicsHandler(AnalysisService analysisService, TelegramApiProvider telegramApiProvider,
                          ChatDAO chatDAO, TopicDAO topicDAO,
                          ResourceBundleService resourceBundleService) {
    this.analysisService = analysisService;
    this.telegramApiProvider = telegramApiProvider;
    this.chatDAO = chatDAO;
    this.topicDAO = topicDAO;
    this.resourceBundleService = resourceBundleService;
  }

  @Override
  public void handle(Update update) {
    Message message = update.getMessage();

    if (message == null)
      return;

    Chat chat = chatDAO.find(message.getChatId());
    String result = getTopicsMessage(chat);
    telegramApiProvider.sendMessage(message.getChat(), result);
  }

  private String getTopicsMessage(Chat chat) {
    List<Topic> humanTopics = getHumanTopics(chat);
    Set<String> autoTopics = getAutoTopics(chat);
    List<String> hashTags = analysisService.getChatHashTags(chat);

    if ((humanTopics == null || humanTopics.isEmpty()) && (autoTopics == null || autoTopics.isEmpty()))
      return resourceBundleService.getMessage(chat.getLanguageShort(), "no.topics.message");


    int count = 0;
    StringBuilder sb = new StringBuilder();

    if (humanTopics != null) {
      sb.append(resourceBundleService.getMessage(chat.getLanguageShort(), "topics.header1.message"));
      for (Topic t : humanTopics) {
        sb.append(++count).append(". ").append(t.getText()).append("\n");
      }
    }

    if (autoTopics != null) {
      count = 0;

      if (humanTopics != null) {
        sb.append(resourceBundleService.getMessage(chat.getLanguageShort(), "topics.header2.message"));
      } else {
        sb.append(resourceBundleService.getMessage(chat.getLanguageShort(), "topics.header3.message"));
      }

      for (String t : autoTopics) {
        sb.append(++count).append(". ").append(t).append("\n");
      }
    }

    if (hashTags != null && !hashTags.isEmpty()) {
      sb.append(resourceBundleService.getMessage(chat.getLanguageShort(), "popular.hashtags"))
          .append(String.join(", ", hashTags));
    }

    return sb.toString();
  }

  private List<Topic> getHumanTopics(Chat chat) {
    return topicDAO.find(chat, chat.getRebirthDate());

  }

  private Set<String> getAutoTopics(Chat chat) {
    List<String> keywords = analysisService.getChatKeywords(chat);
    return analysisService.getChatTopics(keywords, chat.getLanguage());
  }
}
