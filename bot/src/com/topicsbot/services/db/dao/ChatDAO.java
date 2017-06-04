package com.topicsbot.services.db.dao;

import com.topicsbot.model.ChannelType;
import com.topicsbot.model.chat.Chat;
import com.topicsbot.model.chat.ChatLanguage;
import com.topicsbot.model.chat.ChatType;
import com.topicsbot.services.db.DBService;
import com.topicsbot.services.db.query.ChatQuery;

import java.time.LocalDate;
import java.time.ZoneId;

/**
 * author: Artem Voronov
 */
public class ChatDAO {

  private DBService db;

  public ChatDAO(DBService db) {
    this.db = db;
  }

  public Chat create(String externalId, String title, ChannelType channel, ChatType type, ChatLanguage language,
                     int size, ZoneId timezone, LocalDate rebirthDate) {
    return db.tx(s -> {
      Chat chat = new Chat();
      chat.setExternalId(externalId);
      chat.setTitle(title);
      chat.setChannel(channel);
      chat.setType(type);
      chat.setLanguage(language);
      chat.setSize(size);
      chat.setTimezone(timezone);
      chat.setRebirthDate(rebirthDate);
      s.save(chat);
      return chat;
    });
  }

  public void update(String externalId, ChatLanguage language) {
    db.vtx(s -> {
      Chat chat = (Chat) ChatQuery.byTelegramExternalId(externalId, s).uniqueResult();
      chat.setLanguage(language);
      s.saveOrUpdate(chat);
    });
  }

  public void update(String externalId, ZoneId timezone) {
    db.vtx(s -> {
      Chat chat = (Chat) ChatQuery.byTelegramExternalId(externalId, s).uniqueResult();
      chat.setTimezone(timezone);
      s.saveOrUpdate(chat);
    });
  }

  public Chat find(String externalId) {
    return db.tx(s -> (Chat) ChatQuery.byTelegramExternalId(externalId, s).uniqueResult());
  }

  public void delete(String externalId) {
    db.vtx(s -> {
      Chat chat = (Chat) ChatQuery.byTelegramExternalId(externalId, s).uniqueResult();
      s.delete(chat);
    });
  }
}
