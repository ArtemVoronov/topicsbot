package com.topicsbot.services.api.telegram.handlers.user;

import com.topicsbot.model.chat.Chat;
import com.topicsbot.services.analysis.AnalysisService;
import com.topicsbot.services.api.telegram.TelegramApiProvider;
import com.topicsbot.services.api.telegram.handlers.UpdateHandler;
import com.topicsbot.services.api.telegram.model.Message;
import com.topicsbot.services.api.telegram.model.Update;
import com.topicsbot.services.db.dao.ChatDAO;
import com.topicsbot.services.db.dao.TopicDAO;
import com.topicsbot.services.i18n.ResourceBundleService;

/**
 * Author: Artem Voronov
 */
public class GetStatisticsHandler implements UpdateHandler {
  private final AnalysisService analysisService;
  private final TelegramApiProvider telegramApiProvider;
  private final ChatDAO chatDAO;
  private final TopicDAO topicDAO;
  private final ResourceBundleService resourceBundleService;

  public GetStatisticsHandler(AnalysisService analysisService, TelegramApiProvider telegramApiProvider,
                              ChatDAO chatDAO, TopicDAO topicDAO,
                              ResourceBundleService resourceBundleService) {
    this.analysisService = analysisService;
    this.telegramApiProvider = telegramApiProvider;
    this.chatDAO = chatDAO;
    this.topicDAO = topicDAO;
    this.resourceBundleService = resourceBundleService;
  }

  @Override
  public void handle(Update update) {
    Message message = update.getMessage();

    if (message == null)
      return;

    Chat chat = chatDAO.find(message.getChatId());
    String result = getStatisticsMessage(chat);
    telegramApiProvider.sendMessage(message.getChat(), result);
  }

  private String getStatisticsMessage(Chat chat) {
    return "TODO";//TODO
  }

//  public String getChatStatisticsMessage(Long chatId, boolean extended) throws IOException {
//    DayStatistics dayStatistics = TopicsBotCore.INSTANCE.getDayStatistics(chatId);
//    StringBuilder sb = new StringBuilder(TopicsBotCore.INSTANCE.getMessageLocalization(chatId, MessageType.STATISTICS_HEADER));
//
//    if (dayStatistics == null) {
//
//      sb.append(TopicsBotCore.INSTANCE.getMessageLocalization(chatId, MessageType.MESSAGES_COUNT))
//          .append(0)
//          .append(TopicsBotCore.INSTANCE.getMessageLocalization(chatId, MessageType.WORDS_COUNT))
//          .append(0)
//          .append(TopicsBotCore.INSTANCE.getMessageLocalization(chatId, MessageType.STICKERS_COUNT))
//          .append(0)
//          .append(TopicsBotCore.INSTANCE.getMessageLocalization(chatId, MessageType.FLOOD_SIZE_COUNT))
//          .append(0);
//      return  sb.toString();
//    } else {
//
//      sb.append(TopicsBotCore.INSTANCE.getMessageLocalization(chatId, MessageType.MESSAGES_COUNT))
//          .append(dayStatistics.getMessageCounter())
//          .append(TopicsBotCore.INSTANCE.getMessageLocalization(chatId, MessageType.WORDS_COUNT))
//          .append(dayStatistics.getWordCounter())
//          .append(TopicsBotCore.INSTANCE.getMessageLocalization(chatId, MessageType.STICKERS_COUNT))
//          .append(dayStatistics.getStickerCounter())
//          .append(TopicsBotCore.INSTANCE.getMessageLocalization(chatId, MessageType.FLOOD_SIZE_COUNT))
//          .append(dayStatistics.getFloodSize())
//          .append("\n");
//
//      String userStatistics = getUserStatisticsMessage(chatId, dayStatistics, extended);
//
//      if (!"".equals(userStatistics)) {
//        sb.append("\n").append(userStatistics);
//      }
//
//      List<String> keywords = getChatKeywordsFrequency(chatId, dayStatistics.getCreatedOn(), false);
//
//      if (keywords != null && !keywords.isEmpty()) {
//        String keywordStatistics = String.join(", ", keywords);
//        sb.append(TopicsBotCore.INSTANCE.getMessageLocalization(chatId, MessageType.TOP_KEYWORDS)).append(keywordStatistics);
//      }
//
//      return sb.toString();
//    }
//  }
//
//  private String getUserStatisticsMessage(Long chatId, DayStatistics dayStatistics, boolean extended) {
//    StringBuilder sb = new StringBuilder();
//
//    if (dayStatistics == null) {
//      logger.error("Null day statistics for chat: " + chatId);
//      return "";
//    }
//
//    Map<Long, UserDayStatistics> flooders = TopicsBotCore.INSTANCE.getUserDayStatistics(chatId);
//
//    if (flooders == null) {
//      return "";
//    }
//
//    flooders.values().stream()
//        .sorted((o1, o2) -> Long.compare(o2.getFloodSize(), o1.getFloodSize()))
//        .limit(10)
//        .forEach(stat -> {
//
//          float percentFlood = (float) stat.getFloodSize() / dayStatistics.getFloodSize() * 100;
//
//          if (Float.isNaN(percentFlood)) {
//            percentFlood = 0;
//          }
//
//          if (percentFlood != 0) {
//            sb.append(stat.getUserFullName()).append(": ");
//            sb.append(String.format("%.2f", percentFlood)).append(" %");
//
//            if (extended) {
//              sb.append(" (").append(stat.getFloodSize()).append(")");
//            }
//
//            sb.append("\n");
//          }
//        });
//
//
//    if (sb.length() != 0) {
//      sb.insert(0, TopicsBotCore.INSTANCE.getMessageLocalization(chatId, MessageType.FLOODERS_OLYMP_HEADER));
//    }
//
//    return sb.toString();
//  }

}
