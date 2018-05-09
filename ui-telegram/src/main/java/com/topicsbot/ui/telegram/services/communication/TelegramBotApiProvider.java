package com.topicsbot.ui.telegram.services.communication;

import com.topicsbot.ui.telegram.services.communication.model.Updates;

/**
 * Author: Artem Voronov
 */
public interface TelegramBotApiProvider {

  Updates getUpdates();
  Updates getUpdates(int offset);

}