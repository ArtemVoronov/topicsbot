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
  //TODO: clean
//  void deleteIndexes(Chat chat, LocalDate date);
//  void renameIndexes(Chat chat, LocalDate from, LocalDate to);
//  void swap(Chat chat, LocalDate from, LocalDate to);
  List<String> getChatKeywords(Chat chat);
  Set<String> getChatTopics(List<String> keywords, ChatLanguage language);
  List<String> getChatHashTags(Chat chat);
  List<String> getWorldKeywords(String dateIsoFormatted, ChatLanguage chatLanguage);
  Set<String> getWorldTopics(List<String> keywords, ChatLanguage language);
  List<String> getWorldHashTags(String dateIsoFormatted, ChatLanguage chatLanguage);
}
