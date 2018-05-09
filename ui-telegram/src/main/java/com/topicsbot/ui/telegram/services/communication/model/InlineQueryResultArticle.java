package com.topicsbot.ui.telegram.services.communication.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Author: Artem Voronov
 */
public class InlineQueryResultArticle implements InlineQueryResult {

  private String type = "article";

  private String id;

  private String title;

  @JsonProperty("input_message_content")
  private InputTextMessageContent inputMessageContent;

  private String description;

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public InputTextMessageContent getInputMessageContent() {
    return inputMessageContent;
  }

  public void setInputMessageContent(InputTextMessageContent inputMessageContent) {
    this.inputMessageContent = inputMessageContent;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }
}
