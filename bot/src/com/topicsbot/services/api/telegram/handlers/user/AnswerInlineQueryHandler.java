package com.topicsbot.services.api.telegram.handlers.user;

import com.topicsbot.model.chat.ChatLanguage;
import com.topicsbot.services.api.telegram.TelegramApiProvider;
import com.topicsbot.services.api.telegram.handlers.UpdateHandler;
import com.topicsbot.services.api.telegram.model.*;
import com.topicsbot.services.messages.MessagesFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: Artem Voronov
 */
public class AnswerInlineQueryHandler implements UpdateHandler {
  private final TelegramApiProvider telegramApiProvider;
  private final MessagesFactory messagesFactory;

  public AnswerInlineQueryHandler(TelegramApiProvider telegramApiProvider, MessagesFactory messagesFactory) {
    this.telegramApiProvider = telegramApiProvider;
    this.messagesFactory = messagesFactory;
  }

  @Override
  public void handle(Update update) {
    InlineQuery inlineQuery = update.getInlineQuery();

    if (inlineQuery == null)
      return;

    List<InlineQueryResult> result = createInlineQueryResults();
    telegramApiProvider.answerInlineQuery(inlineQuery.getId(), result);
  }

  private List<InlineQueryResult> createInlineQueryResults() {
    String worldTopicsMessage = messagesFactory.getWorldTopicsMessage(ChatLanguage.EN);

    InputTextMessageContent worldTopicsResult = new InputTextMessageContent();
    worldTopicsResult.setMessageText(worldTopicsMessage);

    InlineQueryResultArticle worldTopicsArticle = new InlineQueryResultArticle();
    worldTopicsArticle.setId("get_world_topics_en");
    worldTopicsArticle.setTitle("Topics");
    worldTopicsArticle.setDescription("See what has been discussed in Telegram today");
    worldTopicsArticle.setInputMessageContent(worldTopicsResult);


    List<InlineQueryResult> inlineQueryResults = new ArrayList<>();
    inlineQueryResults.add(worldTopicsArticle);

    return inlineQueryResults;
  }
}