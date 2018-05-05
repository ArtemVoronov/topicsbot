package com.topicsbot.model.services.db

import com.topicsbot.model.services.db.KVStorageService

class TestKVStorageServiceFactory {

  synchronized static KVStorageService createKVStorageService() throws IOException {
    return new KVStorageServiceMock()
  }

  private static class KVStorageServiceMock implements KVStorageService {
    private final Map<String, String> kvStorage = new HashMap<>()

    @Override
    void destroy() {
      kvStorage.clear()
    }

    @Override
    String get(String key) {
      return kvStorage.get(key)
    }

    @Override
    void set(String key, String value) {
      kvStorage.put(key, value)
    }
  }
}
