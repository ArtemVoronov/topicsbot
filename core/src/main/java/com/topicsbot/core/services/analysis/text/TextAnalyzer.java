package com.topicsbot.core.services.analysis.text;


import com.topicsbot.model.entities.chat.Chat;
import com.topicsbot.model.entities.chat.ChatLanguage;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Author: Artem Voronov
 */
public interface TextAnalyzer {

  void indexMessage(String message, Chat chat);

  List<String> getChatKeywords(Chat chat);

  List<String> getChatHashTags(Chat chat);

  List<String> getWorldKeywords(LocalDate date, ChatLanguage language);

  List<String> getWorldHashTags(LocalDate date, ChatLanguage language);

  Map<String, Long> getChatKeywordsWithFrequency(Chat chat, LocalDate date);

  Map<String, Long> getChatKeywordsWithFrequency(Chat chat, LocalDate from, LocalDate till);
}
