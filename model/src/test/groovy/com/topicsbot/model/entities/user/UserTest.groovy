package com.topicsbot.model.entities.user

import com.topicsbot.model.entities.DBTestBase
import com.topicsbot.model.entities.chat.ChannelType
import org.hibernate.exception.ConstraintViolationException
import org.junit.Test

import static org.junit.Assert.assertNotNull

/**
 * author: Artem Voronov
 */
class UserTest extends DBTestBase {

  static User createUser(Map overrides = [:]) {
    def defaultFields = [
        externalId : 'test1234567890',
        name : 'John Doe',
        channel: ChannelType.TELEGRAM
    ]
    return new User(defaultFields + overrides)
  }

  @Test
  void testSaveAndLoad() {
    def user = createUser()

    db.tx { s ->
      s.save(user)
    }

    assertNotNull(user.id)

    def loaded = db.tx { s -> s.get(User, user.id) as User}

    assertNotNull(loaded)
    assertUsersEquals(user, loaded)
  }

  @Test
  void testDuplicate() {
    def user = createUser()
    def duplicate = createUser()

    def msg = shouldFail(ConstraintViolationException) {
      db.tx { s ->
        s.save(user)
        s.save(duplicate)
      }
    }

    assertEquals "could not execute statement", msg
  }

  static void assertUsersEquals(User expected, User actual) {
    assertEquals expected.name, actual.name
    assertEquals expected.externalId, actual.externalId
    assertEquals expected.channel, actual.channel
  }

}
