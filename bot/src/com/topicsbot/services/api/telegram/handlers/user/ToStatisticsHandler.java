package com.topicsbot.services.api.telegram.handlers.user;

import com.topicsbot.model.chat.Chat;
import com.topicsbot.services.analysis.AnalysisService;
import com.topicsbot.services.api.telegram.handlers.UpdateHandler;
import com.topicsbot.services.api.telegram.model.Message;
import com.topicsbot.services.api.telegram.model.Update;
import com.topicsbot.services.db.dao.ChatDAO;

/**
 * Author: Artem Voronov
 */
public class ToStatisticsHandler implements UpdateHandler {

  private final AnalysisService analysisService;
  private final ChatDAO chatDAO;

  public ToStatisticsHandler(AnalysisService analysisService, ChatDAO chatDAO) {
    this.analysisService = analysisService;
    this.chatDAO = chatDAO;
  }

  @Override
  public void handle(Update update) {
    Message message = update.getMessage();

    if (message == null)
      return;

    Chat chat = chatDAO.find(message.getChatId());
    String text = message.getText().trim();
    analysisService.index(text, chat);
  }
}
