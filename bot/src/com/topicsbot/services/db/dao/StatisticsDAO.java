package com.topicsbot.services.db.dao;

import com.topicsbot.model.chat.Chat;
import com.topicsbot.model.statistics.ChatDayStatistics;
import com.topicsbot.model.statistics.Statistics;
import com.topicsbot.model.statistics.UserDayStatistics;
import com.topicsbot.model.user.User;
import com.topicsbot.services.db.DBService;
import com.topicsbot.services.db.query.ChatStatisticsQuery;
import com.topicsbot.services.db.query.UserStatisticsQuery;

import java.time.LocalDate;

/**
 * author: Artem Voronov
 */
public class StatisticsDAO {

  private DBService db;

  public StatisticsDAO(DBService db) {
    this.db = db;
  }

  public ChatDayStatistics create(Chat chat, LocalDate date, int floodSize, int messageCounter, int wordCounter) {
    return db.tx(s -> {
      ChatDayStatistics stat = new ChatDayStatistics();
      stat.setChat(chat);
      stat.setDate(date);
      stat.setFloodSize(floodSize);
      stat.setMessageCounter(messageCounter);
      stat.setWordCounter(wordCounter);
      stat.setStartCommandCounter(0);
      stat.setHelpCommandCounter(0);
      stat.setTopicsCommandCounter(0);
      stat.setAddTopicCommandCounter(0);
      stat.setStatisticsCommandCounter(0);
      stat.setSettingsCommandCounter(0);
      stat.setRateCommandCounter(0);
      stat.setWorldTopicsCommandCounter(0);
      s.save(stat);
      return stat;
    });
  }

  public UserDayStatistics create(Chat chat, User user, LocalDate date, int floodSize, int messageCounter, int wordCounter) {
    return db.tx(s -> {
      UserDayStatistics stat = new UserDayStatistics();
      stat.setChat(chat);
      stat.setUser(user);
      stat.setDate(date);
      stat.setFloodSize(floodSize);
      stat.setMessageCounter(messageCounter);
      stat.setWordCounter(wordCounter);
      stat.setStartCommandCounter(0);
      stat.setHelpCommandCounter(0);
      stat.setTopicsCommandCounter(0);
      stat.setAddTopicCommandCounter(0);
      stat.setStatisticsCommandCounter(0);
      stat.setSettingsCommandCounter(0);
      stat.setRateCommandCounter(0);
      stat.setWorldTopicsCommandCounter(0);
      s.save(stat);
      return stat;
    });
  }

  public ChatDayStatistics find(Chat chat, LocalDate date) {
    return db.tx(s -> (ChatDayStatistics) ChatStatisticsQuery.byChat(chat, date, s).uniqueResult());
  }

  public UserDayStatistics find(Chat chat, User user, LocalDate date) {
    return db.tx(s -> (UserDayStatistics) UserStatisticsQuery.byUser(chat, user, date, s).uniqueResult());
  }

  public void update(Statistics detached, int floodSize, int messageCounter, int wordCounter) {
    db.vtx(s -> {
      Statistics stat = (Statistics) s.merge(detached);
      stat.setFloodSize(stat.getFloodSize() + floodSize);
      stat.setMessageCounter(stat.getMessageCounter() + messageCounter);
      stat.setWordCounter(stat.getWordCounter() + wordCounter);
    });
  }

}
