package com.topicsbot.model.user

import com.topicsbot.model.ChannelType
import com.topicsbot.model.DBTestBase

import static org.junit.Assert.assertNotNull

/**
 * author: Artem Voronov
 */
class UserTest extends DBTestBase {

  static User createCorrectUser(Map overrides = [:]) {
    def defaultFields = [
      externalId : '-6301029591',
      name       : 'Artem Voronov',
      type       : ChannelType.TELEGRAM
    ]
    return new User(defaultFields + overrides)
  }

  void testSaveAndLoad() {
    def user = createCorrectUser()

    tx { s -> s.save(user) }

    assertNotNull user.id

    def another = tx { s -> s.get(User, user.id) as User}

    assertNotNull another
    assertUsersEquals user, another
  }

  static void assertUsersEquals(User expected, User actual) {
    assertEquals expected.channel, actual.channel
    assertEquals expected.externalId, actual.externalId
    assertEquals expected.name, actual.name
  }
}
