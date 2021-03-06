package com.topicsbot.services.api.telegram.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Author: Artem Voronov
 */
public class ChatInfo {
  @JsonProperty("result")
  private Chat chat;

  public Chat getChat() {
    return chat;
  }

  public void setChat(Chat chat) {
    this.chat = chat;
  }
}
