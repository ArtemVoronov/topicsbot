package com.topicsbot.services.cache;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.topicsbot.model.TimeZones;
import com.topicsbot.model.chat.Chat;
import com.topicsbot.model.chat.ChatLanguage;
import com.topicsbot.model.chat.ChatType;
import com.topicsbot.model.statistics.ChatDayStatistics;
import com.topicsbot.model.statistics.CounterType;
import com.topicsbot.model.statistics.Statistics;
import com.topicsbot.model.statistics.UserDayStatistics;
import com.topicsbot.model.user.User;
import com.topicsbot.services.db.DBService;
import com.topicsbot.services.db.query.ChatQuery;
import org.apache.log4j.Logger;
import org.hibernate.Session;

import java.io.File;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

/**
 * Author: Artem Voronov
 */
public class CacheService {
  private static final Logger logger = Logger.getLogger("CACHE_SERVICE");
  private static final String ADD_TOPIC_WAITERS_JSON_FILE = "/add_topics_waiters.json";
  private static final String CHAT_STATISTICS_JSON_FILE = "/chat_statistics.json";
  private static final String USER_STATISTICS_JSON_FILE = "/user_statistics.json";
  private static final ObjectMapper MAPPER = new ObjectMapper();
  static {
    MAPPER.registerModule(new JavaTimeModule());
    MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  }
  private final Map<String, Set<String>> addTopicWaiters;//chat -> set of external ids (of users)
  private final String pathCacheDir;//chat -> set of external ids (of users)

  private final Map<String, ChatDayStatistics> chatStatistics; //chat external id + chat rebirth date -> stat
  private final Map<String, Map<String, UserDayStatistics>> userStatistics; //chat external id + chat rebirth date -> user external id -> stat
  private final ReentrantReadWriteLock statisticsLock = new ReentrantReadWriteLock(); //for statistics
  private final Lock statisticsRead = statisticsLock.readLock();
  private final Lock statisticsWrite = statisticsLock.writeLock();
  private final ReentrantReadWriteLock waitersLock = new ReentrantReadWriteLock(); //for add topic waiters
  private final Lock waitersRead = waitersLock.readLock();
  private final Lock waitersWrite = waitersLock.writeLock();

  private final DBService db;
  private final StatisticsCacheCleaner statisticsCacheCleaner;

  public CacheService(String pathCacheDir, DBService db, ScheduledExecutorService scheduledExecutorService) {
    this.pathCacheDir = pathCacheDir;
    this.addTopicWaiters = initAddTopicWaiters(pathCacheDir);
    this.chatStatistics = initChatStatistics(pathCacheDir);
    this.userStatistics = initUserStatistics(pathCacheDir);
    this.db = db;
    this.statisticsCacheCleaner = new StatisticsCacheCleaner();
    scheduledExecutorService.scheduleWithFixedDelay(statisticsCacheCleaner, 60L, 3600L*24, TimeUnit.SECONDS);
  }

  private Map<String, Set<String>> initAddTopicWaiters(String pathCacheDir) {
    try {
      File file = new File(pathCacheDir + ADD_TOPIC_WAITERS_JSON_FILE);
      return !file.exists() ? new HashMap<>() : MAPPER.readValue(file, new TypeReference<Map<String, Set<String>>>() {});
    } catch (Exception ex) {
      throw new IllegalStateException("Unable to read ADD_TOPIC_WAITERS_JSON_FILE");
    }
  }

  private Map<String, ChatDayStatistics> initChatStatistics(String pathCacheDir) {
    try {
      File file = new File(pathCacheDir + CHAT_STATISTICS_JSON_FILE);
      return !file.exists() ? new HashMap<>() : MAPPER.readValue(file, new TypeReference<Map<String, ChatDayStatistics>>() {});
    } catch (Exception ex) {
      throw new IllegalStateException("Unable to read CHAT_STATISTICS_JSON_FILE");
    }
  }

  private Map<String, Map<String, UserDayStatistics>> initUserStatistics(String pathCacheDir) {
    try {
      File file = new File(pathCacheDir + USER_STATISTICS_JSON_FILE);
      return !file.exists() ? new HashMap<>() : MAPPER.readValue(file, new TypeReference<Map<String, Map<String, UserDayStatistics>>>() {});
    } catch (Exception ex) {
      throw new IllegalStateException("Unable to read USER_STATISTICS_JSON_FILE");
    }
  }

  public void shutdown() {
    storeWaiters();
    storeChatsStatistics();
    storeUserStatistics();
  }

  private void storeWaiters() {
    try {
      MAPPER.writeValue(new File(pathCacheDir + ADD_TOPIC_WAITERS_JSON_FILE), addTopicWaiters);
    } catch (Exception ex) {
      logger.error("Unable to read ADD_TOPIC_WAITERS_JSON_FILE:\n" + toJsonString(addTopicWaiters), ex);
    }
  }

  private void storeChatsStatistics() {
    try {
      MAPPER.writeValue(new File(pathCacheDir + CHAT_STATISTICS_JSON_FILE), chatStatistics);
    } catch (Exception ex) {
      logger.error("Unable to read CHAT_STATISTICS_JSON_FILE", ex);
    }
  }

  private void storeUserStatistics() {
    try {
      MAPPER.writeValue(new File(pathCacheDir + USER_STATISTICS_JSON_FILE), userStatistics);
    } catch (Exception ex) {
      logger.error("Unable to read USER_STATISTICS_JSON_FILE", ex);
    }
  }

  public boolean hasWaiters(String chatId) {
    try {
      waitersRead.lock();
      return addTopicWaiters.containsKey(chatId) && !addTopicWaiters.get(chatId).isEmpty();
    } finally {
      waitersRead.unlock();
    }
  }

  public boolean hasWaiter(String chatId, String userId) {
    try {
      waitersRead.lock();
      return hasWaiters(chatId) && addTopicWaiters.get(chatId).contains(userId);
    } finally {
      waitersRead.unlock();
    }
  }

  public void addWaiter(String chatId, String userId) {
    try {
      waitersWrite.lock();

      Set<String> waiters = addTopicWaiters.get(chatId);
      if (waiters == null) {
        waiters = new HashSet<>();
        addTopicWaiters.put(chatId, waiters);
      }
      waiters.add(userId);

    } finally {
      waitersWrite.unlock();
    }
  }

  public void removeWaiter(String chatId, String userId) {
    try {
      waitersWrite.lock();

      Set<String> waiters = addTopicWaiters.get(chatId);
      if (waiters == null)
        return;

      waiters.remove(userId);
    } finally {
      waitersWrite.unlock();
    }
  }

  public ChatDayStatistics getChatStatistics(String chatExternalId, LocalDate date) {
    try {
      statisticsRead.lock();
      return chatStatistics.get(chatExternalId + "_" + date);
    } finally {
      statisticsRead.unlock();
    }
  }

  private static ChatDayStatistics createEmptyChatDayStatistics(Chat chat) {
    ChatDayStatistics stat = new ChatDayStatistics();
    stat.setChat(chat);
    stat.setCreateDate(chat.getRebirthDate());
    return stat;
  }

  private static UserDayStatistics createEmptyUserDayStatistics(Chat chat, User user) {
    UserDayStatistics stat = new UserDayStatistics();
    stat.setChat(chat);
    stat.setCreateDate(chat.getRebirthDate());
    stat.setUser(user);
    return stat;
  }

  public void createChatStatistics(Chat chat, CounterType counterType, int counterValue) {
    try {
      statisticsWrite.lock();
      ChatDayStatistics stat = createEmptyChatDayStatistics(chat);
      setCounterValue(stat, counterType, counterValue);
      chatStatistics.put(getChatKey(chat), stat);
    } finally {
      statisticsWrite.unlock();
    }
  }

  private static int getCounterValue(Statistics stat, CounterType counterType) {
    switch (counterType) {
      case FLOOD:
        return stat.getFloodSize() == null ? 0 : stat.getFloodSize();
      case MESSAGES:
        return stat.getMessageCounter() == null ? 0 : stat.getMessageCounter();
      case WORDS:
        return stat.getWordCounter() == null ? 0 : stat.getWordCounter();
      case START_COMMAND:
        return stat.getStartCommandCounter() == null ? 0 : stat.getStartCommandCounter();
      case TOPICS_COMMAND:
        return stat.getTopicsCommandCounter() == null ? 0 : stat.getTopicsCommandCounter();
      case STATISTICS_COMMAND:
        return stat.getStatisticsCommandCounter() == null ? 0 : stat.getStatisticsCommandCounter();
      case ADD_TOPIC_COMMAND:
        return stat.getAddTopicCommandCounter() == null ? 0 : stat.getAddTopicCommandCounter();
      case WORLD_TOPICS_COMMAND:
        return stat.getWorldTopicsCommandCounter() == null ? 0 : stat.getWorldTopicsCommandCounter();
      case CANCEL_COMMAND:
        return stat.getCancelCommandCounter() == null ? 0 : stat.getCancelCommandCounter();
      case SETTINGS_COMMAND:
        return stat.getSettingsCommandCounter() == null ? 0 : stat.getSettingsCommandCounter();
      case HELP_COMMAND:
        return stat.getHelpCommandCounter() == null ? 0 : stat.getHelpCommandCounter();
      case RATE_COMMAND:
        return stat.getRateCommandCounter() == null ? 0 : stat.getRateCommandCounter();
      case RANK_COMMAND:
        return stat.getRankCommandCounter() == null ? 0 : stat.getRankCommandCounter();
      case DONATE_COMMAND:
        return stat.getDonateCommandCounter() == null ? 0 : stat.getDonateCommandCounter();
      default:
        throw new IllegalArgumentException("unknown counter type");
    }
  }

  private static void setCounterValue(Statistics stat, CounterType counterType, int counterValue) {
    switch (counterType) {
      case FLOOD:
        stat.setFloodSize(counterValue);
        break;
      case MESSAGES:
        stat.setMessageCounter(counterValue);
        break;
      case WORDS:
        stat.setWordCounter(counterValue);
        break;
      case START_COMMAND:
        stat.setStartCommandCounter(counterValue);
        break;
      case TOPICS_COMMAND:
        stat.setTopicsCommandCounter(counterValue);
        break;
      case STATISTICS_COMMAND:
        stat.setStatisticsCommandCounter(counterValue);
        break;
      case ADD_TOPIC_COMMAND:
        stat.setAddTopicCommandCounter(counterValue);
        break;
      case WORLD_TOPICS_COMMAND:
        stat.setWorldTopicsCommandCounter(counterValue);
        break;
      case CANCEL_COMMAND:
        stat.setCancelCommandCounter(counterValue);
        break;
      case SETTINGS_COMMAND:
        stat.setSettingsCommandCounter(counterValue);
        break;
      case HELP_COMMAND:
        stat.setHelpCommandCounter(counterValue);
        break;
      case RATE_COMMAND:
        stat.setRateCommandCounter(counterValue);
        break;
      case RANK_COMMAND:
        stat.setRankCommandCounter(counterValue);
        break;
      case DONATE_COMMAND:
        stat.setDonateCommandCounter(counterValue);
        break;
      default:
        throw new IllegalArgumentException("unknown counter type");
    }
  }

  public UserDayStatistics getUserStatistics(Chat chat, User user) {
    try {
      statisticsRead.lock();
      Map<String, UserDayStatistics> flooders = userStatistics.get(getChatKey(chat));
      return flooders != null ? flooders.get(user.getExternalId()) : null;

    } finally {
      statisticsRead.unlock();
    }
  }

  public Map<String, UserDayStatistics> getFlooders(String chatExternalId, LocalDate date) {
    try {
      statisticsRead.lock();
      return userStatistics.get(chatExternalId + "_" + date);
    } finally {
      statisticsRead.unlock();
    }
  }

  public void createUserStatistics(Chat chat, User user, CounterType counterType, int counterValue) {
    try {
      statisticsWrite.lock();
      UserDayStatistics stat = createEmptyUserDayStatistics(chat, user);
      setCounterValue(stat, counterType, counterValue);

      String chatKey = getChatKey(chat);
      Map<String, UserDayStatistics> flooders = userStatistics.get(chatKey);

      if (flooders == null) {
        flooders = new HashMap<>();
        userStatistics.put(chatKey, flooders);
      }

      flooders.put(user.getExternalId(), stat);

    } finally {
      statisticsWrite.unlock();
    }
  }

  public void updateStatistics(Statistics stat, CounterType counterType, int counterValue) {
    try {
      statisticsWrite.lock();
      int previousCounterValue = getCounterValue(stat, counterType);
      setCounterValue(stat, counterType, previousCounterValue+counterValue);
    } finally {
      statisticsWrite.unlock();
    }
  }

  StatisticsCacheCleaner getStatisticsCacheCleaner() {
    return statisticsCacheCleaner;
  }

  public CacheInfo getCacheInfo(LocalDate localDate) {
    try {
      statisticsRead.lock();

      Set<ChatDayStatistics> activeChats = chatStatistics.values().stream()
          .filter(v-> v.getCreateDate() != null && v.getCreateDate().isEqual(localDate))
          .collect(Collectors.toSet());
      Set<UserDayStatistics> activeUsers = userStatistics.values().stream()
          .flatMap(map -> map.values().stream())
          .filter(v-> v.getCreateDate() != null && v.getCreateDate().isEqual(localDate))
          .collect(Collectors.toSet());

      Map<String, Integer> chatTypes = createEmptyChatTypesMap();
      Map<String, Integer> chatLanguages = createEmptyChatLanguagesMap();
      Map<String, Integer> chatTimeZones = createEmptyChatTimezonesMap();
      Map<String, Integer> chatCounters = createEmptyChatCountersMap();

      Map<String, Map<String, Integer>> chatLanguagesDetailed = createEmptyChatLanguagesDatailedMap();
      Map<String, Map<String, Integer>> chatTimeZonesDetailed = createEmptyChatTimezonesDatailedMap();
      Map<String, Map<String, Integer>> chatCountersDetailed = createEmptyChatCountersDatailedMap();

      CounterType[] counterTypes = CounterType.values();
      for (ChatDayStatistics stat : activeChats) {
        Chat chat = stat.getChat();

        //chatTypes
        String chatType = chat.getType().name();
        Integer chatTypesCount = chatTypes.get(chatType);
        chatTypes.put(chatType, chatTypesCount + 1);

        //chatTimeZones
        String prettyTimezone = TimeZones.mappingFrom.get(chat.getTimezone().toString());
        chatTimeZones.put(prettyTimezone, chatTimeZones.get(prettyTimezone) + 1);

        //chatLanguages
        String language = chat.getLanguage().name();
        chatLanguages.put(language, chatLanguages.get(language) + 1);

        //chatCounters
        //chatCountersDetailed
        for (CounterType counterType : counterTypes) {
          String counterTypeName = counterType.name();
          Integer counterFromMap = chatCounters.get(counterTypeName);
          chatCounters.put(counterTypeName, counterFromMap + getCounterValue(stat, counterType));

          Map<String, Integer> detailedCounter = chatCountersDetailed.get(counterTypeName);
          detailedCounter.put(chatType, detailedCounter.get(chatType) + getCounterValue(stat, counterType));
        }

        //chatLanguagesDetailed
        Map<String, Integer> detailedLanguagesCount = chatLanguagesDetailed.get(language);
        detailedLanguagesCount.put(chatType, detailedLanguagesCount.get(chatType) + 1);

        //chatTimeZonesDetailed
        Map<String, Integer> detailedTimezonesCount = chatTimeZonesDetailed.get(prettyTimezone);
        detailedTimezonesCount.put(chatType, detailedTimezonesCount.get(chatType) + 1);
      }

      return new CacheInfo(activeChats.size(), activeUsers.size(), chatTypes, chatLanguages, chatTimeZones, chatCounters, chatLanguagesDetailed, chatTimeZonesDetailed, chatCountersDetailed);

    } finally {
      statisticsRead.unlock();
    }
  }

  private static Map<String, Integer> createEmptyChatTypesMap() {
    Map<String, Integer> result = new HashMap<>(4);
    for (ChatType t : ChatType.values()) {
      result.put(t.name(), 0);
    }
    return result;
  }

  private static Map<String, Integer> createEmptyChatLanguagesMap() {
    Map<String, Integer> result = new HashMap<>(15);
    for (ChatLanguage t : ChatLanguage.values()) {
      result.put(t.name(), 0);
    }
    return result;
  }

  private static Map<String, Integer> createEmptyChatTimezonesMap() {
    Map<String, Integer> result = new HashMap<>(26);
    for (String t : TimeZones.mappingTo.keySet()) {
      result.put(t, 0);
    }
    return result;
  }

  private static Map<String, Integer> createEmptyChatCountersMap() {
    Map<String, Integer> result = new HashMap<>(13);
    for (CounterType t : CounterType.values()) {
      result.put(t.name(), 0);
    }
    return result;
  }

  private static Map<String, Map<String, Integer>> createEmptyChatLanguagesDatailedMap() {
    Map<String, Map<String, Integer>> result = new HashMap<>(15);
    for (ChatLanguage t : ChatLanguage.values()) {
      result.put(t.name(), createEmptyChatTypesMap());
    }
    return result;
  }

  private static Map<String, Map<String, Integer>> createEmptyChatTimezonesDatailedMap() {
    Map<String, Map<String, Integer>> result = new HashMap<>(26);
    for (String t : TimeZones.mappingTo.keySet()) {
      result.put(t, createEmptyChatTypesMap());
    }
    return result;
  }

  private static Map<String, Map<String, Integer>> createEmptyChatCountersDatailedMap() {
    Map<String,  Map<String, Integer>> result = new HashMap<>(13);
    for (CounterType t : CounterType.values()) {
      result.put(t.name(), createEmptyChatTypesMap());
    }
    return result;
  }

  private static String toJsonString(Map<String, Set<String>> data) {
    return "{\n"+data.entrySet().stream().map(s-> "\"" + s.getKey() + "\" : [\"" + String.join("\", \"", s.getValue()) + "\"]").collect(Collectors.joining(",\n"))+"\n}";
  }

  private static String getChatKey(Chat chat) {
    return chat.getExternalId() + "_" + chat.getRebirthDate();
  }

  private class StatisticsCacheCleaner implements Runnable {

    @Override
    @SuppressWarnings("unchecked")
    public void run() {
      try {
        statisticsWrite.lock();

        db.vtx(s -> {
          List<Chat> all = ChatQuery.all(s).list();

          for (Chat chat : all) {
            cleanChatStatistics(chat, s);
            cleanUserStatistics(chat, s);
          }
        });

      } catch (Exception ex) {
        logger.error(ex.getMessage(), ex);
      } finally {
        statisticsWrite.unlock();
      }
    }

    private void cleanChatStatistics(Chat chat, Session session) {
      Set<String> keys = chatStatistics.keySet().stream().filter(stat -> stat.startsWith(chat.getExternalId())).collect(Collectors.toSet());
      String currentStatKey = getChatKey(chat);
      keys.remove(currentStatKey);

      for (String key : keys) {
        ChatDayStatistics statistics = chatStatistics.get(key);
        if (statistics != null) {
          session.save(statistics);
          chatStatistics.remove(key);
        }
      }
    }

    private void cleanUserStatistics(Chat chat, Session session) {
      Set<String> keys = userStatistics.keySet().stream().filter(stat -> stat.startsWith(chat.getExternalId())).collect(Collectors.toSet());
      String currentStatKey = getChatKey(chat);
      keys.remove(currentStatKey);

      for (String key : keys) {
        Map<String, UserDayStatistics> flooders = userStatistics.get(key);
        if (flooders != null) {
          for (UserDayStatistics statistics : flooders.values()) {
            session.save(statistics);
          }
          userStatistics.remove(key);
        }
      }
    }

  }
}
