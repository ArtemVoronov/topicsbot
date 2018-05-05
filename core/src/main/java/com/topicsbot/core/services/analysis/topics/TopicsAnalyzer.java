package com.topicsbot.core.services.analysis.topics;

import com.topicsbot.model.entities.chat.ChatLanguage;

import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * Author: Artem Voronov
 */
public interface TopicsAnalyzer {
  Set<String> getTopics(List<String> keywords, ChatLanguage language) throws IOException;
}
