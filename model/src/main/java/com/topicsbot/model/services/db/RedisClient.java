package com.topicsbot.model.services.db;

import org.apache.commons.configuration2.Configuration;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.time.Duration;

/**
 * Author: Artem Voronov
 */
public class RedisClient implements KeyValueStorageService {
  private final JedisPool jedisPool;

  public RedisClient(Configuration jedisPoolProperties) {
    JedisPoolConfig poolConfig = createJedisPoolConfig(jedisPoolProperties);
    this.jedisPool = new JedisPool(poolConfig, jedisPoolProperties.getString("jedis.connection.url"));
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

  private static JedisPoolConfig createJedisPoolConfig(Configuration jedisPoolProperties) {
    return createPoolConfig(
        jedisPoolProperties.getInt("jedis.pool.max.active"),
        jedisPoolProperties.getInt("jedis.pool.max.idle"),
        jedisPoolProperties.getInt("jedis.pool.min.idle"),
        jedisPoolProperties.getBoolean("jedis.pool.test.on.borrow "),
        jedisPoolProperties.getBoolean("jedis.pool.test.on.return"),
        jedisPoolProperties.getBoolean("jedis.pool.test.while.idle"),
        jedisPoolProperties.getLong("jedis.pool.mine.victable.idle.time.seconds"),
        jedisPoolProperties.getLong("jedis.pool.time.between.eviction.runs.seconds"),
        jedisPoolProperties.getInt("jedis.pool.num.tests.per.eviction.run"),
        jedisPoolProperties.getBoolean("jedis.pool.block.when.exhausted")
    );
  }

  //see GenericObjectPool.class for details
  private static JedisPoolConfig createPoolConfig(int maxActive, int maxIdle, int minIdle,
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
