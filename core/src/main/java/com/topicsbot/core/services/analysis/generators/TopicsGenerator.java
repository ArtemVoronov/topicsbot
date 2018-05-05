package com.topicsbot.core.services.analysis.generators;

import com.topicsbot.model.entities.chat.ChatLanguage;

import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * Author: Artem Voronov
 */
public interface TopicsGenerator {
  Set<String> getTopics(List<String> keywords, ChatLanguage language) throws IOException;
}
