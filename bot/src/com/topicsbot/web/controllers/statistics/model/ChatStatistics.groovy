package com.topicsbot.web.controllers.statistics.model

/**
 * Author: Artem Voronov
 */
class ChatStatistics {
  int users
  int flood
  int messages
  int words
  List<UserStatistics> userStatistics
  List<KeywordStatistics> keywords
}
