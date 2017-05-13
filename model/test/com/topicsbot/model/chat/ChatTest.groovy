package com.topicsbot.model.chat

import com.topicsbot.model.ChannelType
import com.topicsbot.model.DBTestBase
import com.topicsbot.model.TimeZones

import java.time.LocalDate
import java.time.ZoneId

import static org.junit.Assert.assertNotNull

/**
 * Author: Artem Voronov
 */
class ChatTest extends DBTestBase {
  static Chat createCorrectChat(Map overrides = [:]) {
    def defaultFields = [
      externalId        : '-630123892091',
      title             : 'some title',
      type              : ChannelType.TELEGRAM,
      size              : ChatSize.PRIVATE,
      language          : ChatLanguage.EN,
      chatMembersCount  : 1,
      timezone          : ZoneId.of(TimeZones.GMT_0.name),
      rebirthDate       : LocalDate.now()
    ]
    return new Chat(defaultFields + overrides)
  }

  void testSaveAndLoad() {
    def chat = createCorrectChat()

    tx { s -> s.save(chat) }

    assertNotNull chat.id

    def another = tx { s -> s.get(Chat, chat.id) as Chat}

    assertNotNull another
    assertChatsEquals chat, another
  }

  static void assertChatsEquals(Chat expected, Chat actual) {
    assertEquals expected.type, actual.type
    assertEquals expected.externalId, actual.externalId
    assertEquals expected.size, actual.size
    assertEquals expected.title, actual.title
    assertEquals expected.language, actual.language
    assertEquals expected.chatMembersCount, actual.chatMembersCount
    assertEquals expected.timezone, actual.timezone
    assertEquals expected.rebirthDate, actual.rebirthDate
  }
}
