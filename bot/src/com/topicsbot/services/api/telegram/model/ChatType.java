package com.topicsbot.services.api.telegram.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Author: Artem Voronov
 */
public enum ChatType {
  @JsonProperty("private")
  PRIVATE,
  @JsonProperty("group")
  GROUP,
  @JsonProperty("supergroup")
  SUPERGROUP,
  @JsonProperty("channel")
  CHANNEL,
}
