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
public class ChatController {

  private DBService db;

  public ChatController(DBService db) {
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

  public void update(String externalId) {
    db.vtx(s -> {
      Chat chat = (Chat) ChatQuery.byExternalId(externalId, s).uniqueResult();
      //TODO
      s.save(chat);
    });
  }

  public Chat find(String externalId) {
    return db.tx(s -> (Chat) ChatQuery.byExternalId(externalId, s).uniqueResult());
  }

  public void delete(String externalId) {
    db.vtx(s -> {
      Chat chat = (Chat) ChatQuery.byExternalId(externalId, s).uniqueResult();
      s.delete(chat);
    });
  }
}
