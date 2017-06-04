package com.topicsbot.services.api.telegram.handlers.user;

import com.topicsbot.model.TimeZones;
import com.topicsbot.model.chat.Chat;
import com.topicsbot.services.api.telegram.TelegramApiProvider;
import com.topicsbot.services.api.telegram.handlers.KeyboardFactory;
import com.topicsbot.services.api.telegram.handlers.UpdateHandler;
import com.topicsbot.services.api.telegram.model.Message;
import com.topicsbot.services.api.telegram.model.Update;
import com.topicsbot.services.db.dao.ChatDAO;
import com.topicsbot.services.i18n.ResourceBundleService;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

/**
 * Author: Artem Voronov
 */
public class ChangeTimezoneHandler implements UpdateHandler {

  private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

  private final ChatDAO chatDAO;
  private final TelegramApiProvider telegramApiProvider;
  private final ResourceBundleService resourceBundleService;

  public ChangeTimezoneHandler(ChatDAO chatDAO, TelegramApiProvider telegramApiProvider, ResourceBundleService resourceBundleService) {
    this.chatDAO = chatDAO;
    this.telegramApiProvider = telegramApiProvider;
    this.resourceBundleService = resourceBundleService;
  }

  @Override
  public void handle(Update update) {
    Message message = update.getMessage();

    if (message == null)
      return;


    Chat chat = chatDAO.find(message.getChatId());

    String text = message.getText().trim();
    String[] tokens = text.split(KeyboardFactory.SECRET_COMMAND_TIME_PARAM);
    if (tokens.length < 2)
      return;

    String chosenTimezone = tokens[1].trim();
    ZoneId newTimezone = convert(chosenTimezone);

    chatDAO.update(chat.getExternalId(), newTimezone);

    String template = resourceBundleService.getMessage(chat.getLanguageShort(), "time.was.chosen.message");
    String feedback = String.format(template, chosenTimezone, LocalTime.now(newTimezone).format(TIME_FORMATTER));
    telegramApiProvider.hideKeyboard(message.getChat(), feedback, KeyboardFactory.createHideKeyboard());
  }

  //TODO: clean
//  private void changeTimezone(Chat chat, ZoneId newTimezone) {
//    LocalDate dateOld = LocalDate.now(chat.getTimezone());
//    LocalDate dateNew = LocalDate.now(newTimezone);
//
//    if (dateNew.isEqual(dateOld)) {
//      chatDAO.update(chat.getExternalId(), newTimezone);
//      return;
//    }
//
//    //если дата изменилась, то в этом случае приоритет отдаётся текущим данным, т.е. всему что юзеры успели ввести
//
//    //удаляем старые индексы и переименовываем новые
////    analysisProvider.deleteIndexes(chat, dateNew);
////    analysisProvider.renameIndexes(chat, dateOld, dateNew);
//    analysisProvider.swap(chat, dateOld, dateNew);
//
//    //если статистика в кэше
//    ChatDayStatistics chatStat = cacheService.removeChatStatistics(chat);
//    if (chatStat != null) {
//      chatStat.setCreateDate(dateNew);
//      cacheService.addChatStatistics(chat.getExternalId(), dateNew, chatStat);
//    }
//
//    Map<String, UserDayStatistics> flooders = cacheService.removeUserStatistics(chat);
//    if (flooders != null) {
//      cacheService.addUserStatistics(chat.getExternalId(), dateNew, flooders);
//    }
//
//    //если статистика в БД
//    statisticsDAO.deleteChatStatisticsIfExists(chat, dateOld);
//    statisticsDAO.deleteUserStatisticsIfExists(chat, dateOld);
//    topicDAO.deleteIfExists(chat, dateOld);
//
//    chatDAO.update(chat.getExternalId(), newTimezone);
//  }

  private static ZoneId convert(String chosenTimezone) {
    String zone = TimeZones.mappingTo.get(chosenTimezone);
    return TimeZone.getTimeZone(zone).toZoneId();
  }
}
