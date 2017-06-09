package com.topicsbot.services.cache;

import java.util.Map;

/**
 * Author: Artem Voronov
 */
public class CacheInfo {

  private int activeChats;
  private int activeUsers;
  private Map<String, Integer> chatTypes;
  private Map<String, Integer> chatLanguages;
  private Map<String, Integer> chatTimeZones;
  private Map<String, Integer> chatCounters;
  private Map<String, Map<String, Integer>> chatLanguagesDetailed;
  private Map<String, Map<String, Integer>> chatTimeZonesDetailed;
  private Map<String, Map<String, Integer>> chatCountersDetailed;

  public CacheInfo(int activeChats, int activeUsers, Map<String, Integer> chatTypes, Map<String, Integer> chatLanguages, Map<String, Integer> chatTimeZones, Map<String, Integer> chatCounters, Map<String, Map<String, Integer>> chatLanguagesDetailed, Map<String, Map<String, Integer>> chatTimeZonesDetailed, Map<String, Map<String, Integer>> chatCountersDetailed) {
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

  public Map<String, Integer> getChatTypes() {
    return chatTypes;
  }

  public void setChatTypes(Map<String, Integer> chatTypes) {
    this.chatTypes = chatTypes;
  }

  public Map<String, Integer> getChatLanguages() {
    return chatLanguages;
  }

  public void setChatLanguages(Map<String, Integer> chatLanguages) {
    this.chatLanguages = chatLanguages;
  }

  public Map<String, Integer> getChatTimeZones() {
    return chatTimeZones;
  }

  public void setChatTimeZones(Map<String, Integer> chatTimeZones) {
    this.chatTimeZones = chatTimeZones;
  }

  public Map<String, Integer> getChatCounters() {
    return chatCounters;
  }

  public void setChatCounters(Map<String, Integer> chatCounters) {
    this.chatCounters = chatCounters;
  }

  public Map<String, Map<String, Integer>> getChatLanguagesDetailed() {
    return chatLanguagesDetailed;
  }

  public void setChatLanguagesDetailed(Map<String, Map<String, Integer>> chatLanguagesDetailed) {
    this.chatLanguagesDetailed = chatLanguagesDetailed;
  }

  public Map<String, Map<String, Integer>> getChatTimeZonesDetailed() {
    return chatTimeZonesDetailed;
  }

  public void setChatTimeZonesDetailed(Map<String, Map<String, Integer>> chatTimeZonesDetailed) {
    this.chatTimeZonesDetailed = chatTimeZonesDetailed;
  }

  public Map<String, Map<String, Integer>> getChatCountersDetailed() {
    return chatCountersDetailed;
  }

  public void setChatCountersDetailed(Map<String, Map<String, Integer>> chatCountersDetailed) {
    this.chatCountersDetailed = chatCountersDetailed;
  }
}
