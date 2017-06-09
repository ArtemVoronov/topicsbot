package com.topicsbot.services.api.telegram.daemons;

import com.topicsbot.model.chat.Chat;
import com.topicsbot.services.api.telegram.TelegramApiProvider;
import com.topicsbot.services.api.telegram.model.ChatType;
import com.topicsbot.services.db.DBService;
import com.topicsbot.services.db.query.ChatQuery;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * Author: Artem Voronov
 */
public class UpdateChatInfoDaemon implements Runnable {
  private static final Logger logger = Logger.getLogger("UPDATE_CHAT_INFO_DAEMON");

  private final DBService db;
  private final TelegramApiProvider telegramApiProvider;

  public UpdateChatInfoDaemon(DBService db, TelegramApiProvider telegramApiProvider) {
    this.db = db;
    this.telegramApiProvider = telegramApiProvider;
  }

  @Override
  @SuppressWarnings("unchecked")
  public void run() {
    try {
      db.vtx(s -> {
        List<Chat> allTelegramChats = ChatQuery.telegram(s).list();

        for (Chat chat : allTelegramChats) {
          com.topicsbot.services.api.telegram.model.Chat apiChat = telegramApiProvider.getChat(chat.getExternalId());
          if (apiChat == null) {
            if (logger.isDebugEnabled())
              logger.debug("Telegram chat cannot be loaded: " + chat.getExternalId());
            continue;
          }
          int size = apiChat.getType() == ChatType.PRIVATE ? 1 : telegramApiProvider.getChatMembersCount(chat.getExternalId());
          chat.setSize(size);
          chat.setTitle(apiChat.getTitle());
          s.saveOrUpdate(chat);
        }
      });
    } catch (Exception ex) {
      logger.error(ex.getMessage(), ex);
    }
  }

}
