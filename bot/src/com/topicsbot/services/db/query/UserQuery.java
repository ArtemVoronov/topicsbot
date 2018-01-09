package com.topicsbot.services.db.query;

import com.topicsbot.model.ChannelType;
import com.topicsbot.model.user.User;
import org.hibernate.CacheMode;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;


/**
 * Author: Artem Voronov
 */
public class UserQuery {
  public static Criteria all(Session s) {
    Criteria c = s.createCriteria(User.class);
    c.setCacheable(true);
    c.setCacheRegion("users");
    c.setCacheMode(CacheMode.NORMAL);
    return c;
  }

  public static Criteria telegram(Session s) {
    return all(s).add(Restrictions.eq("channel", ChannelType.TELEGRAM));
  }

  public static Criteria byId(Integer id, Session s) {
    return all(s).add(Restrictions.idEq(id));
  }

  public static Criteria byExternalId(String externalId, ChannelType channel, Session s) {
    return all(s)
        .add(Restrictions.eq("externalId", externalId))
        .add(Restrictions.eq("channel", channel));
  }
}
