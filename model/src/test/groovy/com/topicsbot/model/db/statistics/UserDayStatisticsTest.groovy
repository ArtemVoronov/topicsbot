package com.topicsbot.model.db.statistics

import com.topicsbot.model.db.DBTestBase
import com.topicsbot.model.db.chat.ChatTest
import com.topicsbot.model.db.user.UserTest
import org.junit.Test

import java.time.LocalDate

import static org.junit.Assert.assertNotNull

/**
 * Author: Artem Voronov
 */
class UserDayStatisticsTest extends DBTestBase {

  static UserDayStatistics createUserDayStatistics(Map overrides = [:]) {
    def defaultFields = [
      chat                        : ChatTest.createChat(),
      user                        : UserTest.createUser(),
      createDate                  : LocalDate.now()
    ]
    return new UserDayStatistics(defaultFields + overrides)
  }

  @Test
  void testSaveAndLoad() {
    def stat = createUserDayStatistics()

    db.tx { s ->
      s.save(stat.chat)
      s.save(stat.user)
      s.save(stat)
    }

    assertNotNull stat.id

    def loaded = db.tx { s -> s.get(UserDayStatistics, stat.id) as UserDayStatistics}

    assertNotNull loaded
    assertChatDayStatisticsEquals(stat, loaded)
  }

  static void assertChatDayStatisticsEquals(UserDayStatistics expected, UserDayStatistics actual) {
    ChatDayStatisticsTest.assertChatDayStatisticsEquals expected, actual
    UserTest.assertUsersEquals expected.user, actual.user
  }
}