package com.topicsbot.model.db.user

import com.topicsbot.model.db.DBTestBase
import org.junit.Test

import static org.junit.Assert.assertNotNull

/**
 * author: Artem Voronov
 */
class UserTest extends DBTestBase {

  static User createCorrectUser(Map overrides = [:]) {
    def defaultFields = [
        name : 'Bob'
    ]
    return new User(defaultFields + overrides)
  }

  @Test
  void testSaveAndLoad() {
    def user = createCorrectUser()

    db.tx { s ->
      s.save(user)
    }

    assertNotNull(user.id)

    def user1 = db.tx { s -> s.get(User, user.id) as User}

    assertNotNull(user1)
    assertUsersEquals(user, user1)
  }

  static void assertUsersEquals(User expected, User actual) {
    assertEquals expected.name, actual.name
  }

}
