package com.topicsbot.services.api.telegram.handlers.user;

import com.topicsbot.model.chat.Chat;
import com.topicsbot.services.analysis.AnalysisService;
import com.topicsbot.services.api.telegram.TelegramApiProvider;
import com.topicsbot.services.api.telegram.handlers.UpdateHandler;
import com.topicsbot.services.api.telegram.model.Message;
import com.topicsbot.services.api.telegram.model.Update;
import com.topicsbot.services.db.dao.ChatController;
import com.topicsbot.services.i18n.ResourceBundleService;

import java.util.List;
import java.util.Set;

/**
 * Author: Artem Voronov
 */
public class GetTopicsHandler implements UpdateHandler {
  private final AnalysisService analysisService;
  private final TelegramApiProvider telegramApiProvider;
  private final ChatController chatController;
  private final ResourceBundleService resourceBundleService;

  public GetTopicsHandler(AnalysisService analysisService, TelegramApiProvider telegramApiProvider,
                          ChatController chatController, ResourceBundleService resourceBundleService) {
    this.analysisService = analysisService;
    this.telegramApiProvider = telegramApiProvider;
    this.chatController = chatController;
    this.resourceBundleService = resourceBundleService;
  }

  @Override
  public void handle(Update update) {
    Message message = update.getMessage();

    if (message == null)
      return;

    Chat chat = chatController.find(message.getChatId());
    List<String> keywords = analysisService.getKeywords(chat);
    Set<String> topics = analysisService.getTopics(keywords, chat.getLanguage());

    StringBuilder sb = new StringBuilder();

    int count = 0;
    for (String s : topics) {
      sb.append(++count).append(s).append("\n");
    }

    telegramApiProvider.sendMessage(message.getChat(), sb.toString());
  }

  //TODO:
//  public String getTopicsMessage(Long chatId, boolean isAdmin) throws IOException {
//    Chat current = TopicsBotCore.INSTANCE.getChat(chatId);
//    List<Topic> humanTopics = getHumanTopics(chatId);
//    Set<String> autoTopics = getAutoTopics(chatId, current);
//    List<String> hashTags = getChatHastagsFrequency(chatId, current.getBirthday(), isAdmin);
//
//    if (humanTopics == null && autoTopics == null && (hashTags == null || hashTags.isEmpty())) {
//      return TopicsBotCore.INSTANCE.getMessageLocalization(chatId, MessageType.NO_TOPICS);
//    }
//
//    int count = 0;
//    StringBuilder sb = new StringBuilder();
//
//    if (humanTopics != null) {
//      sb.append( TopicsBotCore.INSTANCE.getMessageLocalization(chatId, MessageType.TOPICS_HEADER1) );
//      for (Topic t : humanTopics) {
//        sb.append(++count).append(". ").append(t.getText()).append("\n");
//      }
//    }
//
//    if (autoTopics != null) {
//      count = 0;
//
//      if (humanTopics != null) {
//        sb.append( TopicsBotCore.INSTANCE.getMessageLocalization(chatId, MessageType.TOPICS_HEADER2) );
//      } else {
//        sb.append( TopicsBotCore.INSTANCE.getMessageLocalization(chatId, MessageType.TOPICS_HEADER3) );
//      }
//
//      for (String t : autoTopics) {
//        sb.append(++count).append(". ").append(t).append("\n");
//      }
//    }
//
//    if (hashTags != null && !hashTags.isEmpty()) {
//      String chatHashTagsMessage = String.join(", ", hashTags);
//      sb.append(TopicsBotCore.INSTANCE.getMessageLocalization(chatId, MessageType.POPULAR_HASHTAGS)).append(chatHashTagsMessage);
//    }
//
//    return sb.toString();
//  }
}
