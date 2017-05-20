package com.topicsbot.services.api.telegram.handlers;

import com.topicsbot.services.api.telegram.model.Update;

/**
 * Author: Artem Voronov
 */
public interface UpdateHandler {

  void handle(Update update);
}
