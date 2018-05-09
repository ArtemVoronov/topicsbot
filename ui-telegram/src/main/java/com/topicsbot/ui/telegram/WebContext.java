package com.topicsbot.ui.telegram;

import com.topicsbot.ui.telegram.services.Services;
import org.apache.commons.configuration2.Configuration;

import javax.enterprise.context.ApplicationScoped;

/**
 * author: Artem Voronov
 */
@ApplicationScoped
public class WebContext {

  private static Services services;

  static synchronized void init(Configuration config) {
    if (WebContext.services == null) {
      WebContext.services = new Services(config);
    }
  }

  static synchronized void stop() {
    if (services != null)
      services.shutdown();
  }

}
