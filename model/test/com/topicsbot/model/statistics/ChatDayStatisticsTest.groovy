package com.topicsbot.model.statistics

import com.topicsbot.model.DBTestBase
import com.topicsbot.model.chat.ChatTest

import java.time.LocalDate

import static org.junit.Assert.assertNotNull

/**
 * Author: Artem Voronov
 */
class ChatDayStatisticsTest extends DBTestBase {

  static ChatDayStatistics createCorrectChatDayStatistics(Map overrides = [:]) {
    def defaultFields = [
      chat                        : ChatTest.createCorrectChat(),
      date                        : LocalDate.now(),
      floodSize                   : 500,
      messageCounter              : 15,
      wordCounter                 : 100,
      startCommandCounter         : 1,
      helpCommandCounter          : 1,
      topicsCommandCounter        : 1,
      addTopicCommandCounter      : 1,
      statisticsCommandCounter    : 1,
      settingsCommandCounter      : 1,
      rateCommandCounter          : 1,
      worldTopicsCommandCounter   : 1
    ]
    return new ChatDayStatistics(defaultFields + overrides)
  }

  void testSaveAndLoad() {
    def chatDayStatistics = createCorrectChatDayStatistics()

    tx { s ->
      s.save(chatDayStatistics.chat)
      s.save(chatDayStatistics)
    }

    assertNotNull chatDayStatistics.id

    def another = tx { s -> s.get(ChatDayStatistics, chatDayStatistics.id) as ChatDayStatistics}

    assertNotNull another
    assertChatDayStatisticssEquals chatDayStatistics, another
  }

  static void assertChatDayStatisticssEquals(ChatDayStatistics expected, ChatDayStatistics actual) {
    assertEquals expected.date, actual.date
    ChatTest.assertChatsEquals expected.chat, actual.chat
    assertEquals expected.floodSize, actual.floodSize
    assertEquals expected.messageCounter, actual.messageCounter
    assertEquals expected.wordCounter, actual.wordCounter
    assertEquals expected.startCommandCounter, actual.startCommandCounter
    assertEquals expected.helpCommandCounter, actual.helpCommandCounter
    assertEquals expected.topicsCommandCounter, actual.topicsCommandCounter
    assertEquals expected.addTopicCommandCounter, actual.addTopicCommandCounter
    assertEquals expected.statisticsCommandCounter, actual.statisticsCommandCounter
    assertEquals expected.settingsCommandCounter, actual.settingsCommandCounter
    assertEquals expected.rateCommandCounter, actual.rateCommandCounter
    assertEquals expected.worldTopicsCommandCounter, actual.worldTopicsCommandCounter
  }
}