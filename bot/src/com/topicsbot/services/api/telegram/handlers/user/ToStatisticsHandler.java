package com.topicsbot.services.api.telegram.handlers.user;

import com.topicsbot.model.chat.Chat;
import com.topicsbot.services.analysis.AnalysisService;
import com.topicsbot.services.api.telegram.handlers.UpdateHandler;
import com.topicsbot.services.api.telegram.model.Message;
import com.topicsbot.services.api.telegram.model.Update;
import com.topicsbot.services.db.dao.ChatController;

/**
 * Author: Artem Voronov
 */
public class ToStatisticsHandler implements UpdateHandler {

  private final AnalysisService analysisService;
  private final ChatController chatController;

  public ToStatisticsHandler(AnalysisService analysisService, ChatController chatController) {
    this.analysisService = analysisService;
    this.chatController = chatController;
  }

  @Override
  public void handle(Update update) {
    Message message = update.getMessage();

    if (message == null)
      return;

    Chat chat = chatController.find(message.getChatId());
    String text = message.getText().trim();
    analysisService.index(text, chat);
  }
}
