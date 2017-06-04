package com.topicsbot.services.db.query;

import com.topicsbot.model.ChannelType;
import com.topicsbot.model.chat.Chat;
import com.topicsbot.model.statistics.ChatDayStatistics;
import org.hibernate.CacheMode;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import java.time.LocalDate;


/**
 * Author: Artem Voronov
 */
public class ChatStatisticsQuery {
  public static Criteria all(Session s) {
    Criteria c = s.createCriteria(ChatDayStatistics.class);
    c.setCacheable(true);
    c.setCacheRegion("chat_statistics");
    c.setCacheMode(CacheMode.NORMAL);
    return c;
  }

  public static Criteria byId(Integer id, Session s) {
    return all(s).add(Restrictions.idEq(id));
  }

  public static Criteria active(Session s) {
    return all(s)
        .add(Restrictions.eq("deleted", false));
  }

  public static Criteria deleted(Session s) {
    return all(s)
        .add(Restrictions.eq("deleted", true));
  }

  public static Criteria byChat(Chat chat, LocalDate date, Session s) {
    return active(s)
        .add(Restrictions.eq("chat", chat))
        .add(Restrictions.eq("createDate", date));
  }
}
