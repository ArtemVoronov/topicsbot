package com.topicsbot.services.db.query;

import com.topicsbot.model.chat.Chat;
import org.hibernate.CacheMode;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

/**
 * Author: Artem Voronov
 */
public class ChatQuery {
  public static Criteria all(Session s) {
    Criteria c = s.createCriteria(Chat.class);
    c.setCacheable(true);
    c.setCacheRegion("chats");
    c.setCacheMode(CacheMode.NORMAL);
    return c;
  }

  public static Criteria byExternalId(String externalId, Session s) {
    return all(s).add(Restrictions.eq("externalId", externalId));
  }

}
