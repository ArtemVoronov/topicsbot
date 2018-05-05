package com.topicsbot.core.services.analysis;

import com.topicsbot.core.services.analysis.text.TextAnalyzer;
import com.topicsbot.core.services.analysis.text.LuceneAnalyzer;
import com.topicsbot.core.services.analysis.text.daemons.HistoryCleanerDaemon;
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
import java.util.concurrent.TimeUnit;

/**
 * Author: Artem Voronov
 */
public class TopicsBotAnalyzer implements TextAnalyzer, TopicsAnalyzer {

  private final TopicsAnalyzer topicsAnalyzer;
  private final TextAnalyzer textAnalyzer;

  public TopicsBotAnalyzer(ScheduledExecutorService scheduledExecutorService, String pathToStopWordsDir, String pathToLuceneIndexesDir, String pathToWorldLuceneIndexesDir) {

    this.topicsAnalyzer = new WikiMediaStorage();
    this.textAnalyzer = new LuceneAnalyzer(pathToStopWordsDir, pathToLuceneIndexesDir, pathToWorldLuceneIndexesDir);

    scheduledExecutorService.scheduleWithFixedDelay(new HistoryCleanerDaemon(32, pathToLuceneIndexesDir, pathToWorldLuceneIndexesDir), 60L, 432_000L, TimeUnit.SECONDS);//once per 5 days
  }

  @Override
  public void index(String text, Chat chat) {
    textAnalyzer.index(text, chat);
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
  public List<String> getWorldKeywords(String dateIsoFormatted, ChatLanguage language) {
    return textAnalyzer.getWorldKeywords(dateIsoFormatted, language);
  }

  @Override
  public List<String> getWorldHashTags(String dateIsoFormatted, ChatLanguage language) {
    return textAnalyzer.getWorldHashTags(dateIsoFormatted, language);
  }

  @Override
  public Map<String, Long> getChatKeywordsExtended(String chatExternalId, LocalDate date) {
    return textAnalyzer.getChatKeywordsExtended(chatExternalId, date);
  }

  @Override
  public Map<String, Long> getChatKeywordsExtended(String chatExternalId, LocalDate from, LocalDate till) {
    return textAnalyzer.getChatKeywordsExtended(chatExternalId, from, till);
  }

  @Override
  public Set<String> getTopics(List<String> keywords, ChatLanguage language) throws IOException {
    return topicsAnalyzer.getTopics(keywords, language);
  }

}
