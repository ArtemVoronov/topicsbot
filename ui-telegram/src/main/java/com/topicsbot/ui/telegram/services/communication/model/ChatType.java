package com.topicsbot.ui.telegram.services.communication.model;

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
  CHANNEL
}
