package com.topicsbot.model.services.db

class TestKVStorageServiceFactory {

  synchronized static KeyValueStorageService createKVStorageService() throws IOException {
    return new KeyValueStorageServiceMock()
  }

  private static class KeyValueStorageServiceMock implements KeyValueStorageService {
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
