package com.topicsbot.ui.telegram;

import com.topicsbot.model.services.ModelServicesException;
import com.topicsbot.ui.telegram.services.Services;
import org.apache.commons.configuration2.ex.ConfigurationException;

import javax.enterprise.context.ApplicationScoped;

/**
 * author: Artem Voronov
 */
@ApplicationScoped
public class WebContext {

  private static Services services;

  static synchronized void init() throws ModelServicesException, ConfigurationException {
    if (WebContext.services == null) {
      WebContext.services = new Services();
    }
  }

  static synchronized void stop() {
    if (services != null)
      services.shutdown();
  }

}
