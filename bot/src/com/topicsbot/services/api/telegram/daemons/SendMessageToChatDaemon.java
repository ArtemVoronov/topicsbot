package com.topicsbot.services.api.telegram.daemons;

import com.topicsbot.model.chat.Chat;
import com.topicsbot.services.api.telegram.TelegramApiProvider;
import com.topicsbot.services.api.telegram.model.ChatType;
import com.topicsbot.services.db.DBService;
import com.topicsbot.services.db.query.ChatQuery;
import org.apache.log4j.Logger;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Author: Artem Voronov
 */
public class SendMessageToChatDaemon implements Runnable {
  private static final Logger logger = Logger.getLogger("SendMessageToChatDaemon");
  private static final String text = "Hello everyone! This project suspends its work for an indefinite period, perhaps forever, perhaps until the future, where it will be reborn. Thank you for using Topics Bot! Enjoy the last 7 days of bot working!";

  private final DBService db;
  private final TelegramApiProvider telegramApiProvider;

  public SendMessageToChatDaemon(DBService db, TelegramApiProvider telegramApiProvider) {
    this.db = db;
    this.telegramApiProvider = telegramApiProvider;
  }

  @Override
  @SuppressWarnings("unchecked")
  public void run() {
    try {
      db.vtx(s -> {
//        List<Chat> me = ChatQuery.byTelegramExternalId("193062503", s).list();
        List<Chat> allTelegramChats = ChatQuery.groups(s).list();

//        for (Chat chat : me) {
//          telegramApiProvider.sendMessage(chat.getExternalId(), text);
//        }

        for (Chat chat : allTelegramChats) {
          logger.info("chat id: " + chat.getExternalId() + ", title: " + chat.getTitle());
          telegramApiProvider.sendMessage(chat.getExternalId(), text);
        }
      });
    } catch (Exception ex) {
      logger.error(ex.getMessage(), ex);
    }
  }

}
