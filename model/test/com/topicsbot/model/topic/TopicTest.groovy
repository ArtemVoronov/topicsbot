package com.topicsbot.model.topic

import com.topicsbot.model.DBTestBase
import com.topicsbot.model.chat.ChatTest
import com.topicsbot.model.user.UserTest

import java.time.LocalDate

import static org.junit.Assert.assertNotNull

/**
 * Author: Artem Voronov
 */
class TopicTest extends DBTestBase {

  static Topic createCorrectTopic(Map overrides = [:]) {
    def defaultFields = [
      text          : 'some topics',
      chat          : ChatTest.createCorrectChat(),
      author        : UserTest.createCorrectUser(),
      createDate    : LocalDate.now()
    ]
    return new Topic(defaultFields + overrides)
  }

  void testSaveAndLoad() {
    def topic = createCorrectTopic()

    tx { s ->
      s.save(topic.chat)
      s.save(topic.author)
      s.save(topic)
    }

    assertNotNull topic.id

    def another = tx { s -> s.get(Topic, topic.id) as Topic}

    assertNotNull another
    assertTopicsEquals topic, another
  }

  static void assertTopicsEquals(Topic expected, Topic actual) {
    assertEquals expected.text, actual.text
    assertEquals expected.createDate, actual.createDate
    UserTest.assertUsersEquals expected.author, actual.author
    ChatTest.assertChatsEquals expected.chat, actual.chat
  }
}