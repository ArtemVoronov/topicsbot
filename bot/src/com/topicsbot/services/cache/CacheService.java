package com.topicsbot.services.cache;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Author: Artem Voronov
 */
public class CacheService {
  private static final Logger logger = Logger.getLogger("CACHE_SERVICE");
  private static final String ADD_TOPIC_WAITERS_JSON_FILE = "add_topics_waiters.json";
  private static final ObjectMapper MAPPER = new ObjectMapper();
  private final Map<String, Set<String>> addTopicWaiters;//chat -> set of external ids (of users)
  private final String pathCacheDir;//chat -> set of external ids (of users)

  public CacheService(String pathCacheDir) {
    this.pathCacheDir = pathCacheDir;
    this.addTopicWaiters = initAddTopicWaiters(pathCacheDir);
  }

  private Map<String, Set<String>> initAddTopicWaiters(String pathCacheDir) {
    try {
      File addTopicWaitersFile = new File(pathCacheDir + ADD_TOPIC_WAITERS_JSON_FILE);
      return !addTopicWaitersFile.exists() ? new HashMap<>() : MAPPER.readValue(addTopicWaitersFile, new TypeReference<Map<String, Set<String>>>() {});
    } catch (Exception ex) {
      throw new IllegalStateException("Unable to read ADD_TOPIC_WAITERS_JSON_FILE");
    }
  }

  public void shutdown() {
    try {
      MAPPER.writeValue(new File(pathCacheDir + ADD_TOPIC_WAITERS_JSON_FILE), addTopicWaiters);
    } catch (Exception ex) {
      logger.error("Unable to read ADD_TOPIC_WAITERS_JSON_FILE:\n" + toJsonString(addTopicWaiters), ex);
    }
  }

  public boolean hasWaiters(String chatId) {
    return addTopicWaiters.containsKey(chatId) && !addTopicWaiters.get(chatId).isEmpty();
  }

  public boolean hasWaiter(String chatId, String userId) {
    return hasWaiters(chatId) && addTopicWaiters.get(chatId).contains(userId);
  }

  public void addWaiter(String chatId, String userId) {
    Set<String> waiters = addTopicWaiters.get(chatId);
    if (waiters == null) {
      waiters = new HashSet<>();
      addTopicWaiters.put(chatId, waiters);
    }
    waiters.add(userId);
  }

  public void removeWaiter(String chatId, String userId) {
    Set<String> waiters = addTopicWaiters.get(chatId);
    if (waiters == null)
      return;

    waiters.remove(userId);
  }

  private static String toJsonString(Map<String, Set<String>> data) {
    return "{\n"+data.entrySet().stream().map(s-> "\"" + s.getKey() + "\" : [\"" + String.join("\", \"", s.getValue()) + "\"]").collect(Collectors.joining(",\n"))+"\n}";
  }
}
