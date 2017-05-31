package com.topicsbot.services.api.telegram.daemons;

import org.apache.log4j.Logger;

import java.util.Queue;
import java.util.concurrent.ExecutorService;

/**
 * Author: Artem Voronov
 */
public class SendMessageDaemon implements Runnable {
  private static final Logger logger = Logger.getLogger("SEND_MESSAGE_DAEMON");

  private final Queue<Runnable> requests;
  private final ExecutorService executor;

  public SendMessageDaemon(Queue<Runnable> requests, ExecutorService executor) {
    this.requests = requests;
    this.executor = executor;
  }

  @Override
  public void run() {
    try {
      Runnable next = requests.poll();

      if (next != null)
        executor.submit(next);

    } catch (Exception ex) {
      logger.error(ex.getMessage(), ex);
    }
  }
}