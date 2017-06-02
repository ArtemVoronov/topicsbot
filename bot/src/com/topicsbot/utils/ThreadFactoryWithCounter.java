package com.topicsbot.utils;

import java.util.concurrent.ThreadFactory;

/**
 * Author: Artem Voronov
 */
public class ThreadFactoryWithCounter implements ThreadFactory {
  private int counter;
  private String threadNamePrefix;

  public ThreadFactoryWithCounter(String threadNamePrefix, int initialCounter) {
    this.threadNamePrefix = threadNamePrefix;
    this.counter = initialCounter;
  }

  Thread createThread(Runnable r, String threadName) {
    return new Thread(r, threadName);
  }

  public final Thread newThread(Runnable r) {
    return this.createThread(r, this.threadNamePrefix + this.counter++);
  }
}