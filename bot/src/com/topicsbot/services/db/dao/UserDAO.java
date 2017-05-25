package com.topicsbot.services.db.dao;
import com.topicsbot.model.ChannelType;
import com.topicsbot.model.user.User;
import com.topicsbot.services.db.DBService;
import com.topicsbot.services.db.query.UserQuery;

/**
 * author: Artem Voronov
 */
public class UserDAO {

  private DBService db;

  public UserDAO(DBService db) {
    this.db = db;
  }

  public User create(String externalId, String name, ChannelType channel) {
    return db.tx(s -> {
      User user = new User();
      user.setExternalId(externalId);
      user.setName(name);
      user.setChannel(channel);
      s.save(user);
      return user;
    });
  }

  public User find(String externalId, ChannelType channel) {
    return db.tx(s -> (User) UserQuery.byExternalId(externalId, channel, s).list());
  }

  public void delete(int id) {
    db.vtx(s -> {
      User topic = (User) UserQuery.byId(id, s).uniqueResult();
      s.delete(topic);
    });
  }
}
