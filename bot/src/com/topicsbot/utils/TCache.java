package com.topicsbot.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Author: Artem Voronov
 */
public class TCache<K,V> {

  private final Map<K,ValueWrapper> values = new HashMap<>();
  private final long lifetime;

  public TCache(long lifetime) {
    this.lifetime = lifetime;
  }

  private void removeExpiredItems() {
    long now = System.currentTimeMillis();

    List<K> expiredKeys = new ArrayList<>();

    for (Map.Entry<K, ValueWrapper> e : values.entrySet()) {
      if (now - e.getValue().time > lifetime) {
        expiredKeys.add(e.getKey());
      }
    }

    for (K key : expiredKeys) {
      values.remove(key);
    }
  }

  public synchronized boolean putIfNotSet(K key, V value) {
    removeExpiredItems();

    if (values.containsKey(key))
      return false;

    values.put(key, new ValueWrapper(value, System.currentTimeMillis()));
    return true;
  }

  public synchronized void put(K key, V value) {
    removeExpiredItems();
    values.put(key, new ValueWrapper(value, System.currentTimeMillis()));
  }

  public synchronized V get(K key) {
    long now = System.currentTimeMillis();

    ValueWrapper w = values.get(key);

    if (w == null) {
      return null;
    }

    if (now - w.time > lifetime) {
      values.remove(key);
      return null;
    }

    return w.value;
  }

  public synchronized boolean remove(K key) {
    long now = System.currentTimeMillis();

    ValueWrapper w = values.remove(key);
    return w != null && (now - w.time < lifetime);
  }



  private class ValueWrapper {
    final V value;
    final long time;

    public ValueWrapper(V value, long time) {
      this.value = value;
      this.time = time;
    }
  }
}