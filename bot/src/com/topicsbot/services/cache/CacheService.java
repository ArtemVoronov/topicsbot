package com.topicsbot.services.cache;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.topicsbot.model.chat.Chat;
import com.topicsbot.model.statistics.ChatDayStatistics;
import com.topicsbot.model.statistics.Statistics;
import com.topicsbot.model.statistics.UserDayStatistics;
import com.topicsbot.model.user.User;
import com.topicsbot.services.db.DBService;
import com.topicsbot.services.db.query.ChatQuery;
import org.apache.log4j.Logger;
import org.hibernate.Session;

import java.io.File;
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
    scheduledExecutorService.scheduleWithFixedDelay(statisticsCacheCleaner, 10L, 3600L*24, TimeUnit.SECONDS);
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

  public ChatDayStatistics getChatStatistics(Chat chat) {
    try {
      statisticsRead.lock();
      return chatStatistics.get(getChatKey(chat));
    } finally {
      statisticsRead.unlock();
    }
  }

  public void createChatStatistics(Chat chat, int flood, int messages, int words) {
    try {
      statisticsWrite.lock();
      ChatDayStatistics stat = new ChatDayStatistics();
      stat.setChat(chat);
      stat.setCreateDate(chat.getRebirthDate());
      stat.setFloodSize(flood);
      stat.setMessageCounter(messages);
      stat.setWordCounter(words);
      stat.setStartCommandCounter(0);
      stat.setHelpCommandCounter(0);
      stat.setTopicsCommandCounter(0);
      stat.setAddTopicCommandCounter(0);
      stat.setStatisticsCommandCounter(0);
      stat.setSettingsCommandCounter(0);
      stat.setRateCommandCounter(0);
      stat.setWorldTopicsCommandCounter(0);
      chatStatistics.put(getChatKey(chat), stat);
    } finally {
      statisticsWrite.unlock();
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

  public Map<String, UserDayStatistics> getFlooders(Chat chat) {
    try {
      statisticsRead.lock();
      return userStatistics.get(getChatKey(chat));
    } finally {
      statisticsRead.unlock();
    }
  }

  public void createUserStatistics(Chat chat, User user, int flood, int messages, int words) {
    try {
      statisticsWrite.lock();
      UserDayStatistics stat = new UserDayStatistics();
      stat.setChat(chat);
      stat.setUser(user);
      stat.setCreateDate(chat.getRebirthDate());
      stat.setFloodSize(flood);
      stat.setMessageCounter(messages);
      stat.setWordCounter(words);
      stat.setStartCommandCounter(0);
      stat.setHelpCommandCounter(0);
      stat.setTopicsCommandCounter(0);
      stat.setAddTopicCommandCounter(0);
      stat.setStatisticsCommandCounter(0);
      stat.setSettingsCommandCounter(0);
      stat.setRateCommandCounter(0);
      stat.setWorldTopicsCommandCounter(0);

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

  public void updateStatistics(Statistics stat, int flood, int messages, int words) {
    try {
      statisticsWrite.lock();
      stat.setFloodSize(stat.getFloodSize() + flood);
      stat.setMessageCounter(stat.getMessageCounter() + messages);
      stat.setWordCounter(stat.getWordCounter() + words);
    } finally {
      statisticsWrite.unlock();
    }
  }

  //TODO: clean
//  public ChatDayStatistics removeChatStatistics(Chat chat) {
//    try {
//      statisticsWrite.lock();
//      return chatStatistics.remove(getChatKey(chat));
//    } finally {
//      statisticsWrite.unlock();
//    }
//  }
//
//  public void addChatStatistics(String chatExternalId, LocalDate createDate, ChatDayStatistics stat) {
//    try {
//      statisticsWrite.lock();
//      String key = chatExternalId+"_"+createDate;
//      if (chatStatistics.containsKey(key)) {
//        ChatDayStatistics another = chatStatistics.remove(key);
//        another.setDeleted(true);
//        db.vtx(s->s.save(another));
//      }
//      chatStatistics.put(key, stat);
//    } finally {
//      statisticsWrite.unlock();
//    }
//  }
//
//  public Map<String, UserDayStatistics> removeUserStatistics(Chat chat) {
//    try {
//      statisticsWrite.lock();
//      return userStatistics.remove(getChatKey(chat));
//    } finally {
//      statisticsWrite.unlock();
//    }
//  }
//
//
//  public void addUserStatistics(String chatExternalId, LocalDate createDate, Map<String, UserDayStatistics> flooders) {
//    try {
//      statisticsWrite.lock();
//      String key = chatExternalId+"_"+createDate;
//      if (userStatistics.containsKey(key)) {
//
//        Map<String, UserDayStatistics> another = userStatistics.remove(key);
//        db.vtx(s -> {
//          for (UserDayStatistics statistics : another.values()) {
//            statistics.setDeleted(true);
//            s.save(statistics);
//          }
//        });
//
//      }
//      userStatistics.put(key, flooders);
//    } finally {
//      statisticsWrite.unlock();
//    }
//  }

  StatisticsCacheCleaner getStatisticsCacheCleaner() {
    return statisticsCacheCleaner;
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
