package com.topicsbot.services.api.telegram;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import org.apache.log4j.Logger;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Response;

/**
 * Author: Artem Voronov
 */
class TelegramApiClient {

  private static final Logger logger = Logger.getLogger("TELEGRAM_API_SERVICE");
  private static final ObjectMapper mapper = new ObjectMapper();
  private final Client client;

  TelegramApiClient(int connectTimeout, int requestTimeout) {
    JacksonJsonProvider jacksonJsonProvider = new JacksonJaxbJsonProvider().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    ClientConfig config = new ClientConfig(jacksonJsonProvider);
    config.property(ClientProperties.CONNECT_TIMEOUT, connectTimeout);
    config.property(ClientProperties.READ_TIMEOUT, requestTimeout);
    config.property(ClientProperties.USE_ENCODING, "UTF-8");
    this.client = ClientBuilder.newBuilder().withConfig(config).build();
  }

  <T> T makeRequest(String endpoint, String json, Class<T> clazz) {
    if(logger.isDebugEnabled()) {
      logger.debug("Request to: " + endpoint + ". Params: " + json);
    }

    T result = null;
    Invocation invocation = client.target(endpoint).request().buildPost(Entity.entity(json, "application/json"));
    Response response = null;
    try {
      response = invocation.invoke();
      result = response.hasEntity()? response.readEntity(clazz) :null;
      if(result == null)
        throw new IllegalArgumentException("Null response from Telegram API");
    } catch (Exception ex) {
      logger.error("Error! Cause: " + ex.getMessage(), ex);
    } finally {
      if(response != null)
        response.close();
    }

    return result;
  }
}
