package com.topicsbot.model.entities.statistics

import com.topicsbot.model.entities.DBTestBase
import com.topicsbot.model.entities.chat.ChatTest
import org.junit.Test

import java.time.LocalDate

import static org.junit.Assert.assertNotNull

/**
 * Author: Artem Voronov
 */
class ChatDayStatisticsTest extends DBTestBase {

  static ChatDayStatistics createChatDayStatistics(Map overrides = [:]) {
    def defaultFields = [
      chat                        : ChatTest.createChat(),
      createDate                  : LocalDate.now()
    ]
    return new ChatDayStatistics(defaultFields + overrides)
  }

  @Test
  void testSaveAndLoad() {
    def stat = createChatDayStatistics()

    db.tx { s ->
      s.save(stat.chat)
      s.save(stat)
    }

    assertNotNull stat.id

    def loaded = db.tx { s -> s.get(ChatDayStatistics, stat.id) as ChatDayStatistics}

    assertNotNull loaded
    assertChatDayStatisticsEquals(stat, loaded)
  }

  static void assertChatDayStatisticsEquals(Statistics expected, Statistics actual) {
    assertEquals expected.createDate, actual.createDate
    assertEquals expected.deleted, actual.deleted
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
    assertEquals expected.cancelCommandCounter, actual.cancelCommandCounter
    assertEquals expected.donateCommandCounter, actual.donateCommandCounter
    ChatTest.assertChatsEquals expected.chat, actual.chat
  }
}