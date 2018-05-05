package com.topicsbot.model.entities.topics

import com.topicsbot.model.entities.DBTestBase
import com.topicsbot.model.entities.chat.ChatTest
import com.topicsbot.model.entities.topic.Topic
import com.topicsbot.model.entities.user.UserTest
import org.junit.Test

import java.time.LocalDate

import static org.junit.Assert.assertNotNull

/**
 * author: Artem Voronov
 */
class TopicTest extends DBTestBase {

  static Topic createTopic(Map overrides = [:]) {
    def defaultFields = [
        text          : 'some topics',
        chat          : ChatTest.createChat(),
        author        : UserTest.createUser(),
        createDate    : LocalDate.now()
    ]
    return new Topic(defaultFields + overrides)
  }

  @Test
  void testSaveAndLoad() {
    def topic = createTopic()

    db.tx { s ->
      s.save(topic.chat)
      s.save(topic.author)
      s.save(topic)
    }

    assertNotNull(topic.id)

    def loaded = db.tx { s -> s.get(Topic, topic.id) as Topic}

    assertNotNull(loaded)
    assertTopicsEquals(topic, loaded)
  }

  static void assertTopicsEquals(Topic expected, Topic actual) {
    assertEquals expected.text, actual.text
    assertEquals expected.createDate, actual.createDate
    UserTest.assertUsersEquals expected.author, actual.author
    ChatTest.assertChatsEquals expected.chat, actual.chat
  }

}
