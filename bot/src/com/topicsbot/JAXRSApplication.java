package com.topicsbot;

import com.topicsbot.rest.DefaultExceptionMapper;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;


/**
 * Конфигурация JAX-RS.
 */
public class JAXRSApplication extends ResourceConfig {

  public JAXRSApplication() {
    register(new JacksonFeature());
    register(DefaultExceptionMapper.class);
    packages("com.topicsbot");
  }

}
