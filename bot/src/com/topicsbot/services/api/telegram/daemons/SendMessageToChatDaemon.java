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
  private static final String text = "test close message";

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
//        List<Chat> allTelegramChats = ChatQuery.telegram(s).list();
        List<Chat> allTelegramChats = ChatQuery.byTelegramExternalId("193062503", s).list();

        for (Chat chat : allTelegramChats) {
          telegramApiProvider.sendMessage(chat.getExternalId(), text);
        }
      });
    } catch (Exception ex) {
      logger.error(ex.getMessage(), ex);
    }
  }

}
