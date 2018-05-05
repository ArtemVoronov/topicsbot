package com.topicsbot.model.services.db;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.time.Duration;

/**
 * Author: Artem Voronov
 */
public class KeyValueStorageServiceImpl implements KeyValueStorageService {
  private final JedisPoolConfig poolConfig;
  private final JedisPool jedisPool;

  public KeyValueStorageServiceImpl() {//TODO: add configurable constructor
    this.poolConfig = buildPoolConfig();
    this.jedisPool = new JedisPool(poolConfig, "localhost");
  }

  @Override
  public void destroy() {
    jedisPool.destroy();
  }

  @Override
  public String get(String key) {
    try (Jedis jedis = jedisPool.getResource()) {
      return jedis.get(key);
    }
  }

  @Override
  public void set(String key, String value) {
    try (Jedis jedis = jedisPool.getResource()) {
      jedis.set(key, value);
    }
  }

  private static JedisPoolConfig buildPoolConfig() {
    return buildPoolConfig(128, 128, 16,
        true, true, true,
        300, 150,
        3, true);
  }

  //see GenericObjectPool.class for details
  private static JedisPoolConfig buildPoolConfig(int maxActive, int maxIdle, int minIdle,
                                                 boolean testOnBorrow, boolean testOnReturn, boolean testWhileIdle,
                                                 long minEvictableIdleTimeSeconds, long timeBetweenEvictionRunsSeconds,
                                                 int numTestsPerEvictionRun, boolean blockWhenExhausted) {
    final JedisPoolConfig poolConfig = new JedisPoolConfig();
    poolConfig.setMaxTotal(maxActive);
    poolConfig.setMaxIdle(maxIdle);
    poolConfig.setMinIdle(minIdle);
    poolConfig.setTestOnBorrow(testOnBorrow);
    poolConfig.setTestOnReturn(testOnReturn);
    poolConfig.setTestWhileIdle(testWhileIdle);
    poolConfig.setMinEvictableIdleTimeMillis(Duration.ofSeconds(minEvictableIdleTimeSeconds).toMillis());
    poolConfig.setTimeBetweenEvictionRunsMillis(Duration.ofSeconds(timeBetweenEvictionRunsSeconds).toMillis());
    poolConfig.setNumTestsPerEvictionRun(numTestsPerEvictionRun);
    poolConfig.setBlockWhenExhausted(blockWhenExhausted);
    return poolConfig;
  }

}
