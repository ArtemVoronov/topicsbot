package com.topicsbot.services.db.query;

import com.topicsbot.model.chat.Chat;
import com.topicsbot.model.statistics.UserDayStatistics;
import com.topicsbot.model.user.User;
import org.hibernate.CacheMode;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import java.time.LocalDate;


/**
 * Author: Artem Voronov
 */
public class UserStatisticsQuery {
  public static Criteria all(Session s) {
    Criteria c = s.createCriteria(UserDayStatistics.class);
    c.setCacheable(true);
    c.setCacheRegion("user_statistics");
    c.setCacheMode(CacheMode.NORMAL);
    return c;
  }

  public static Criteria byId(Integer id, Session s) {
    return all(s).add(Restrictions.idEq(id));
  }

  public static Criteria byUser(Chat chat, User user, LocalDate date, Session s) {
    return all(s)
        .add(Restrictions.eq("chat", chat))
        .add(Restrictions.eq("user", user))
        .add(Restrictions.eq("date", date));
  }
}
