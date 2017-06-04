package com.topicsbot.services.api.telegram.handlers.user;

import com.topicsbot.model.chat.Chat;
import com.topicsbot.services.api.telegram.TelegramApiProvider;
import com.topicsbot.services.api.telegram.handlers.UpdateHandler;
import com.topicsbot.services.api.telegram.model.Message;
import com.topicsbot.services.api.telegram.model.Update;
import com.topicsbot.services.db.dao.ChatDAO;
import com.topicsbot.services.messages.MessagesFactory;

/**
 * Author: Artem Voronov
 */
public class GetStatisticsHandler implements UpdateHandler {
  private final TelegramApiProvider telegramApiProvider;
  private final ChatDAO chatDAO;
  private final MessagesFactory messagesFactory;

  public GetStatisticsHandler(TelegramApiProvider telegramApiProvider, MessagesFactory messagesFactory,
                              ChatDAO chatDAO) {
    this.telegramApiProvider = telegramApiProvider;
    this.chatDAO = chatDAO;
    this.messagesFactory = messagesFactory;
  }

  @Override
  public void handle(Update update) {
    Message message = update.getMessage();

    if (message == null)
      return;

    Chat chat = chatDAO.find(message.getChatId());
    String result = messagesFactory.getStatisticsMessage(chat, false);
    telegramApiProvider.sendMessage(message.getChat(), result);
  }

}
