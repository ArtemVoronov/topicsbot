package com.topicsbot.model.statistics

import com.topicsbot.model.DBTestBase
import com.topicsbot.model.chat.ChatTest
import com.topicsbot.model.user.UserTest

import java.time.LocalDate

import static org.junit.Assert.assertNotNull

/**
 * Author: Artem Voronov
 */
class UserDayStatisticsTest extends DBTestBase {

  static UserDayStatistics createCorrectUserDayStatistics(Map overrides = [:]) {
    def defaultFields = [
      chat                        : ChatTest.createCorrectChat(),
      user                        : UserTest.createCorrectUser(),
      createDate                  : LocalDate.now(),
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
    return new UserDayStatistics(defaultFields + overrides)
  }

  void testSaveAndLoad() {
    def userDayStatistics = createCorrectUserDayStatistics()

    tx { s ->
      s.save(userDayStatistics.chat)
      s.save(userDayStatistics.user)
      s.save(userDayStatistics)
    }

    assertNotNull userDayStatistics.id

    def another = tx { s -> s.get(UserDayStatistics, userDayStatistics.id) as UserDayStatistics}

    assertNotNull another
    assertUserDayStatisticssEquals userDayStatistics, another
  }

  static void assertUserDayStatisticssEquals(UserDayStatistics expected, UserDayStatistics actual) {
    assertEquals expected.createDate, actual.createDate
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