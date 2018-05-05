package com.topicsbot.core.services.analysis;


import com.topicsbot.model.entities.chat.Chat;
import com.topicsbot.model.entities.chat.ChatLanguage;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Author: Artem Voronov
 */
public interface AnalysisProvider {
  void index(String text, Chat chat);
  List<String> getChatKeywords(Chat chat);
  Map<String, Long> getChatKeywordsExtended(String chatExternalId, LocalDate date);
  Map<String, Long> getChatKeywordsExtended(String chatExternalId, LocalDate from, LocalDate till);
  Set<String> getChatTopics(List<String> keywords, ChatLanguage language) throws IOException;
  List<String> getChatHashTags(Chat chat);
  List<String> getWorldKeywords(String dateIsoFormatted, ChatLanguage chatLanguage);
  Set<String> getWorldTopics(List<String> keywords, ChatLanguage language) throws IOException;
  List<String> getWorldHashTags(String dateIsoFormatted, ChatLanguage chatLanguage);
}
