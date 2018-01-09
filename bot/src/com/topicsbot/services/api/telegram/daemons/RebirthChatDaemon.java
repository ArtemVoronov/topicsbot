package com.topicsbot.services.api.telegram.daemons;

import com.topicsbot.model.chat.Chat;
import com.topicsbot.model.statistics.ChatDayStatistics;
import com.topicsbot.services.db.DBService;
import com.topicsbot.services.db.query.ChatQuery;
import com.topicsbot.services.db.query.ChatStatisticsQuery;
import org.apache.log4j.Logger;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Author: Artem Voronov
 */
public class RebirthChatDaemon implements Runnable {
  private static final Logger logger = Logger.getLogger("REBIRTH_CHAT_DAEMON");

  private final DBService db;

  public RebirthChatDaemon(DBService db) {
    this.db = db;
  }

  @Override
  @SuppressWarnings("unchecked")
  public void run() {
    try {
      db.vtx(s -> {

        List<Chat> all = ChatQuery.all(s).list();

        for (Chat chat : all) {
          try {
            LocalDateTime chatBirthday = LocalDateTime.of(chat.getRebirthDate(), LocalTime.MIDNIGHT);
            LocalDateTime chatRebirthTime = chatBirthday.plusDays(1);
            LocalDateTime currentTimeAtChatTimeZone = LocalDateTime.now(chat.getTimezone());

            if (currentTimeAtChatTimeZone.isAfter(chatRebirthTime) || currentTimeAtChatTimeZone.equals(chatRebirthTime)) {
              chat.setRebirthDate(chatRebirthTime.toLocalDate());

              //при перерождении будем пересчитывать норму флуда
              List<ChatDayStatistics> stats = ChatStatisticsQuery.byChat(chat, s).list();
              double averageFlood = calcAverageFlood(stats);
              chat.setAverageFlood(averageFlood);

              s.save(chat);
            }
          } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
          }
        }
      });
    } catch (Exception ex) {
      logger.error(ex.getMessage(), ex);
    }
  }



  private static double calcAverageFlood(List<ChatDayStatistics> stats) {
    if (stats == null || stats.isEmpty())
      return 0;

    double counter = 0;
    for (ChatDayStatistics s : stats) {
      counter += s.getFloodSize();
    }

    return counter / stats.size();
  }

}
