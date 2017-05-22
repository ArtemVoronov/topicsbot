package com.topicsbot.services.analysis.generators;

import com.topicsbot.model.chat.ChatLanguage;

import java.util.List;
import java.util.Set;

/**
 * Author: Artem Voronov
 */
public interface TopicsGenerator {
  Set<String> getTopics(List<String> keywords, ChatLanguage language);
}
