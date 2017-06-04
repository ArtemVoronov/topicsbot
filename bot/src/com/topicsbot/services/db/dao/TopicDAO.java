package com.topicsbot.services.db.dao;
import com.topicsbot.model.chat.Chat;
import com.topicsbot.model.topic.Topic;
import com.topicsbot.model.user.User;
import com.topicsbot.services.db.DBService;
import com.topicsbot.services.db.query.TopicQuery;

import java.time.LocalDate;
import java.util.List;

/**
 * author: Artem Voronov
 */
public class TopicDAO {

  private DBService db;

  public TopicDAO(DBService db) {
    this.db = db;
  }

  public Topic create(Chat chat, String text, User user, LocalDate createDate) {
    return db.tx(s -> {
      Topic topic = new Topic();
      topic.setChat(chat);
      topic.setText(text);
      topic.setAuthor(user);
      topic.setCreateDate(createDate);
      s.save(topic);
      return topic;
    });
  }

  @SuppressWarnings("unchecked")
  public List<Topic> find(Chat chat, LocalDate createDate) {
    return db.tx(s -> TopicQuery.byChat(chat, createDate, s).list());
  }

  @SuppressWarnings("unchecked")
  public List<Topic> find(Chat chat, LocalDate createDate, User author) {
    return db.tx(s -> TopicQuery.byChat(chat, createDate, author, s).list());
  }

  //TODO: clean
//  @SuppressWarnings("unchecked")
//  public void deleteIfExists(Chat chat, LocalDate createDate) {
//    db.vtx(s -> {
//      List<Topic> topics = TopicQuery.byChat(chat, createDate, s).list();
//      if (topics != null && !topics.isEmpty()) {
//        for (Topic t : topics) {
//          t.setDeleted(true);
//        }
//      }
//    });
//  }
}
