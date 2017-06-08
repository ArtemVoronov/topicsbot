package com.topicsbot.services.api.telegram.handlers.user;

import com.topicsbot.model.chat.Chat;
import com.topicsbot.model.statistics.CounterType;
import com.topicsbot.services.analysis.AnalysisProvider;
import com.topicsbot.services.api.telegram.handlers.UpdateHandler;
import com.topicsbot.services.api.telegram.model.Message;
import com.topicsbot.services.api.telegram.model.Update;
import com.topicsbot.services.cache.CacheService;
import com.topicsbot.services.db.dao.ChatDAO;
import com.topicsbot.services.db.dao.UserDAO;

/**
 * Author: Artem Voronov
 */
public class ToStatisticsUserHandler extends CommonUserHandler implements UpdateHandler{

  private final AnalysisProvider analysisProvider;
  private final ChatDAO chatDAO;

  public ToStatisticsUserHandler(AnalysisProvider analysisProvider, CacheService cache,
                                 ChatDAO chatDAO, UserDAO userDAO) {
    super(cache, userDAO);
    this.analysisProvider = analysisProvider;
    this.chatDAO = chatDAO;
  }

  @Override
  public void handle(Update update) {
    Message message = update.getMessage();

    if (message == null)
      return;

    Chat chat = chatDAO.find(message.getChatId());

    String text = message.getText();

    if (text == null)
      return;

    text = text.trim();
    analysisProvider.index(text, chat);

    int wordsCount = getWordCounter(message);
    int floodSize = getFloodSize(message);

    updateChatCounters(chat, CounterType.FLOOD, floodSize);
    updateChatCounters(chat, CounterType.WORDS, wordsCount);
    updateChatCounters(chat, CounterType.MESSAGES, 1);

    updateUserCounter(message, chat, CounterType.FLOOD, floodSize);
    updateUserCounter(message, chat, CounterType.WORDS, wordsCount);
    updateUserCounter(message, chat, CounterType.MESSAGES, 1);
  }

  private static int getWordCounter(Message message) {
    return !message.hasText() ? 0 : message.getText().split(" ").length;
  }

  private static int getFloodSize(Message message) {
    return !message.hasText() ? 0 : message.getText().replaceAll("\\s+", "").length();
  }
}
