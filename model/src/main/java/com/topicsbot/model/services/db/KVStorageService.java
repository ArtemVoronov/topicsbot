package com.topicsbot.model.services.db;

/**
 * Author: Artem Voronov
 */
public interface KVStorageService {
  void destroy();

  String get(String key);

  void set(String key, String value);
}
