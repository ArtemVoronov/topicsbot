package com.topicsbot.services.api.telegram.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Author: Artem Voronov
 */
public class InputTextMessageContent implements InputMessageContent{

  @JsonProperty("message_text")
  private String messageText;

  public String getMessageText() {
    return messageText;
  }

  public void setMessageText(String messageText) {
    this.messageText = messageText;
  }
}
