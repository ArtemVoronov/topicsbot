package com.topicsbot.model.db.chat

import com.topicsbot.model.db.DBTestBase
import org.hibernate.exception.ConstraintViolationException
import org.junit.Test

import java.time.LocalDate
import java.time.ZoneId

import static org.junit.Assert.assertNotNull

/**
 * author: Artem Voronov
 */
class ChatTest extends DBTestBase {

  static Chat createChat(Map overrides = [:]) {
    def defaultFields = [
        externalId        : '-630123892091',
        title             : 'some title',
        channel           : ChannelType.TELEGRAM,
        type              : ChatType.PRIVATE,
        language          : ChatLanguage.EN,
        size              : 1,
        timezone          : ZoneId.of(TimeZones.GMT_0.name),
        rebirthDate       : LocalDate.now()
    ]
    return new Chat(defaultFields + overrides)
  }

  @Test
  void testSaveAndLoad() {
    def chat = createChat()

    db.tx { s ->
      s.save(chat)
    }

    assertNotNull(chat.id)

    def loaded = db.tx { s -> s.get(Chat, chat.id) as Chat}

    assertNotNull(loaded)
    assertChatsEquals(chat, loaded)
  }

  @Test
  void testDuplicate() {
    def chat = createChat()
    def duplicate = createChat()

    def msg = shouldFail(ConstraintViolationException) {
      db.tx { s ->
        s.save(chat)
        s.save(duplicate)
      }
    }

    assertEquals "could not execute statement", msg
  }

  static void assertChatsEquals(Chat expected, Chat actual) {
    assertEquals expected.type, actual.type
    assertEquals expected.externalId, actual.externalId
    assertEquals expected.size, actual.size
    assertEquals expected.title, actual.title
    assertEquals expected.language, actual.language
    assertEquals expected.channel, actual.channel
    assertEquals expected.timezone, actual.timezone
    assertEquals expected.rebirthDate, actual.rebirthDate
  }

}
