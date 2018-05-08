package com.topicsbot.core;

import com.topicsbot.core.services.analysis.text.TextAnalyzer;
import com.topicsbot.core.services.analysis.topics.TopicsAnalyzer;

/**
 * Author: Artem Voronov
 */
public interface TopicsBotCore extends TextAnalyzer, TopicsAnalyzer {
}
