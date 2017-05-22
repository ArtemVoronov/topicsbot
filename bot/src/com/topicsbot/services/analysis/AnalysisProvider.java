package com.topicsbot.services.analysis;

import com.topicsbot.model.chat.Chat;
import com.topicsbot.model.chat.ChatLanguage;

import java.util.List;
import java.util.Set;

/**
 * Author: Artem Voronov
 */
public interface AnalysisProvider {
  void index(String text, Chat chat);
  List<String> getKeywords(Chat chat);
  Set<String> getTopics(List<String> keywords, ChatLanguage language);
}
