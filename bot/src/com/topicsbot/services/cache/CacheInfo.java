package com.topicsbot.services.cache;

import com.topicsbot.model.chat.ChatLanguage;
import com.topicsbot.model.chat.ChatType;
import com.topicsbot.model.statistics.CounterType;

import java.util.Map;

/**
 * Author: Artem Voronov
 */
public class CacheInfo {

  private int activeChats;
  private int activeUsers;
  private Map<ChatType, Integer> chatTypes;
  private Map<ChatLanguage, Integer> chatLanguages;
  private Map<String, Integer> chatTimeZones;
  private Map<CounterType, Integer> chatCounters;
  private Map<ChatLanguage, Map<ChatType, Integer>> chatLanguagesDetailed;
  private Map<String, Map<ChatType, Integer>> chatTimeZonesDetailed;
  private Map<CounterType, Map<ChatType, Integer>> chatCountersDetailed;

  public CacheInfo(int activeChats, int activeUsers, Map<ChatType, Integer> chatTypes, Map<ChatLanguage, Integer> chatLanguages, Map<String, Integer> chatTimeZones, Map<CounterType, Integer> chatCounters, Map<ChatLanguage, Map<ChatType, Integer>> chatLanguagesDetailed, Map<String, Map<ChatType, Integer>> chatTimeZonesDetailed, Map<CounterType, Map<ChatType, Integer>> chatCountersDetailed) {
    this.activeChats = activeChats;
    this.activeUsers = activeUsers;
    this.chatTypes = chatTypes;
    this.chatLanguages = chatLanguages;
    this.chatTimeZones = chatTimeZones;
    this.chatCounters = chatCounters;
    this.chatLanguagesDetailed = chatLanguagesDetailed;
    this.chatTimeZonesDetailed = chatTimeZonesDetailed;
    this.chatCountersDetailed = chatCountersDetailed;
  }

  public int getActiveChats() {
    return activeChats;
  }

  public void setActiveChats(int activeChats) {
    this.activeChats = activeChats;
  }

  public int getActiveUsers() {
    return activeUsers;
  }

  public void setActiveUsers(int activeUsers) {
    this.activeUsers = activeUsers;
  }

  public Map<ChatType, Integer> getChatTypes() {
    return chatTypes;
  }

  public void setChatTypes(Map<ChatType, Integer> chatTypes) {
    this.chatTypes = chatTypes;
  }

  public Map<ChatLanguage, Integer> getChatLanguages() {
    return chatLanguages;
  }

  public void setChatLanguages(Map<ChatLanguage, Integer> chatLanguages) {
    this.chatLanguages = chatLanguages;
  }

  public Map<String, Integer> getChatTimeZones() {
    return chatTimeZones;
  }

  public void setChatTimeZones(Map<String, Integer> chatTimeZones) {
    this.chatTimeZones = chatTimeZones;
  }

  public Map<CounterType, Integer> getChatCounters() {
    return chatCounters;
  }

  public void setChatCounters(Map<CounterType, Integer> chatCounters) {
    this.chatCounters = chatCounters;
  }

  public Map<ChatLanguage, Map<ChatType, Integer>> getChatLanguagesDetailed() {
    return chatLanguagesDetailed;
  }

  public void setChatLanguagesDetailed(Map<ChatLanguage, Map<ChatType, Integer>> chatLanguagesDetailed) {
    this.chatLanguagesDetailed = chatLanguagesDetailed;
  }

  public Map<String, Map<ChatType, Integer>> getChatTimeZonesDetailed() {
    return chatTimeZonesDetailed;
  }

  public void setChatTimeZonesDetailed(Map<String, Map<ChatType, Integer>> chatTimeZonesDetailed) {
    this.chatTimeZonesDetailed = chatTimeZonesDetailed;
  }

  public Map<CounterType, Map<ChatType, Integer>> getChatCountersDetailed() {
    return chatCountersDetailed;
  }

  public void setChatCountersDetailed(Map<CounterType, Map<ChatType, Integer>> chatCountersDetailed) {
    this.chatCountersDetailed = chatCountersDetailed;
  }
}
