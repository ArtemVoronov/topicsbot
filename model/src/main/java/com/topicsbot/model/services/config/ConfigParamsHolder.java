package com.topicsbot.model.services.config;

/**
 * author: Artem Voronov
 */
public interface ConfigParamsHolder {
  void shutdown();
  String getConfigParam(String paramName);
}
