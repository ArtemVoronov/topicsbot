package com.topicsbot.model.services;

import java.util.concurrent.ThreadFactory;

/**
 * Author: Artem Voronov
 */
public class ThreadFactoryWithCounter implements ThreadFactory {
  private int counter;
  private String threadNamePrefix;

  ThreadFactoryWithCounter(String threadNamePrefix) {
    this(threadNamePrefix, 0);
  }

  ThreadFactoryWithCounter(String threadNamePrefix, int initialCounter) {
    this.threadNamePrefix = threadNamePrefix;
    this.counter = initialCounter;
  }

  public final Thread newThread(Runnable r) {
    return new Thread(r, threadNamePrefix + counter++);
  }
}
