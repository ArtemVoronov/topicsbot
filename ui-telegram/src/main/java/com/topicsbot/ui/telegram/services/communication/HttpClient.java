package com.topicsbot.ui.telegram.services.communication;

/**
 * Author: Artem Voronov
 */
public abstract class HttpClient {

  public abstract <T> T post(String endpoint, String json, Class<T> clazz);
}