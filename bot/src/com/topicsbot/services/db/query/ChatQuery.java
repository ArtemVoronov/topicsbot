package com.topicsbot.services.db.query;

import com.topicsbot.model.ChannelType;
import com.topicsbot.model.chat.Chat;
import com.topicsbot.model.chat.ChatType;
import org.hibernate.CacheMode;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Restrictions;

/**
 * Author: Artem Voronov
 */
public class ChatQuery {
  public static Criteria all(Session s) {
    Criteria c = s.createCriteria(Chat.class);
//    c.setCacheable(true);
//    c.setCacheRegion("chats");
//    c.setCacheMode(CacheMode.NORMAL);
    return c;
  }

  public static Criteria telegram(Session s) {
    return all(s).add(Restrictions.eq("channel", ChannelType.TELEGRAM));
  }

  public static Criteria byTelegramExternalId(String externalId, Session s) {
    return telegram(s).add(Restrictions.eq("externalId", externalId));
  }

  public static Criteria groups(Session s) {
    Criteria result = telegram(s);
    Disjunction disjunction = Restrictions.disjunction();
    disjunction.add(Restrictions.eq("type", ChatType.GROUP));
    disjunction.add(Restrictions.eq("type", ChatType.SUPER_GROUP));

    result.add(disjunction);

    return result;
  }

}
