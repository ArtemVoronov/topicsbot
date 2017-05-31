package com.topicsbot.services.api.telegram.daemons;

import com.topicsbot.services.api.telegram.TelegramApiProvider;
import com.topicsbot.services.api.telegram.model.Update;
import com.topicsbot.services.api.telegram.model.Updates;
import org.apache.log4j.Logger;

import java.util.Queue;

/**
 * Author: Artem Voronov
 */
public class GetUpdatesDaemon implements Runnable {
  private static final Logger logger = Logger.getLogger("GET_UPDATES_DAEMON");

  private final Queue<Update> updates;
  private final TelegramApiProvider telegramApiProvider;
  private Integer lastUpdateId = null;

  public GetUpdatesDaemon(Queue<Update> updates, TelegramApiProvider telegramApiProvider) {
    this.updates = updates;
    this.telegramApiProvider = telegramApiProvider;
  }

  @Override
  public void run() {
    try {
      Updates result = telegramApiProvider.getUpdates(lastUpdateId);
      if (!result.isEmpty()) {
        lastUpdateId = result.getLastUpdateId() + 1;
        updates.addAll(result.getUpdates());
      }

    } catch (Exception ex) {
      logger.error(ex.getMessage(), ex);
    }
  }
}
