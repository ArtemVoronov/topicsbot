package com.topicsbot.services.api.telegram.handlers.user;

import com.topicsbot.BotContext;
import com.topicsbot.services.api.telegram.TelegramApiProvider;
import com.topicsbot.services.api.telegram.handlers.UpdateHandler;
import com.topicsbot.services.api.telegram.model.Message;
import com.topicsbot.services.api.telegram.model.Update;

/**
 * Author: Artem Voronov
 */
public class StartCommandHandler implements UpdateHandler {
  @Override
  public void handle(Update update) {
    Message message = update.getMessage();
    TelegramApiProvider telegramApiProvider = BotContext.getInstance().getTelegramApiProvider();

    //TODO:
    // 1. get chat lang
    // 2. get start message from resource bundle
    // 3. send message

    telegramApiProvider.sendMessage(message.getChat(), "TODO");
  }
}
