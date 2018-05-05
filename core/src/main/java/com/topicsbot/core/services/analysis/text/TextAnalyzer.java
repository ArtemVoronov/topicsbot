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

  void index(String message, Chat chat);

  List<String> getChatKeywords(Chat chat);

  Map<String, Long> getChatKeywordsExtended(String chatExternalId, LocalDate date);

  Map<String, Long> getChatKeywordsExtended(String chatExternalId, LocalDate from, LocalDate till);

  List<String> getChatHashTags(Chat chat);

  List<String> getWorldKeywords(String dateIsoFormatted, ChatLanguage chatLanguage);

  List<String> getWorldHashTags(String dateIsoFormatted, ChatLanguage chatLanguage);
}
