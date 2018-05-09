package com.topicsbot.core;

import com.topicsbot.core.services.analysis.text.TextAnalyzer;
import com.topicsbot.core.services.analysis.text.LuceneAnalyzer;
import com.topicsbot.core.services.analysis.topics.TopicsAnalyzer;
import com.topicsbot.core.services.analysis.topics.WikiMediaStorage;
import com.topicsbot.model.entities.chat.Chat;
import com.topicsbot.model.entities.chat.ChatLanguage;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Author: Artem Voronov
 */
public class TopicsBotCoreBasic implements TopicsBotCore {

  private final TopicsAnalyzer topicsAnalyzer;
  private final TextAnalyzer textAnalyzer;

  public TopicsBotCoreBasic(String chatLucenePath, String worldLucenePath, ScheduledExecutorService scheduledExecutorService, int historyTimeToLiveInDays) {
    this.topicsAnalyzer = new WikiMediaStorage();
    this.textAnalyzer = new LuceneAnalyzer(chatLucenePath, worldLucenePath, scheduledExecutorService, historyTimeToLiveInDays);
  }

  @Override
  public void indexMessage(String message, Chat chat) {
    textAnalyzer.indexMessage(message, chat);
  }

  @Override
  public List<String> getChatKeywords(Chat chat) {
    return textAnalyzer.getChatKeywords(chat);
  }

  @Override
  public List<String> getChatHashTags(Chat chat) {
    return textAnalyzer.getChatHashTags(chat);
  }

  @Override
  public List<String> getWorldKeywords(LocalDate date, ChatLanguage language) {
    return textAnalyzer.getWorldKeywords(date, language);
  }

  @Override
  public List<String> getWorldHashTags(LocalDate date, ChatLanguage language) {
    return textAnalyzer.getWorldHashTags(date, language);
  }

  @Override
  public Set<String> keywordsToTopics(List<String> keywords, ChatLanguage language) throws IOException {
    return topicsAnalyzer.keywordsToTopics(keywords, language);
  }

  @Override
  public Map<String, Long> getChatKeywordsWithFrequency(Chat chat, LocalDate date) {
    return textAnalyzer.getChatKeywordsWithFrequency(chat, date);
  }

  @Override
  public Map<String, Long> getChatKeywordsWithFrequency(Chat chat, LocalDate from, LocalDate till) {
    return textAnalyzer.getChatKeywordsWithFrequency(chat, from, till);
  }

}
