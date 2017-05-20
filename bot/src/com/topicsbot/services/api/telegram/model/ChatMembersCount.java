package com.topicsbot.services.api.telegram.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Author: Artem Voronov
 */
public class ChatMembersCount {
  @JsonProperty("result")
  private int count;

  public int getCount() {
    return count;
  }

  public void setCount(int count) {
    this.count = count;
  }
}
