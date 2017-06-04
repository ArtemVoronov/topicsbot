package com.topicsbot.services.api.telegram.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Author: Artem Voronov
 */
public class Update {
  @JsonProperty("update_id")
  private int id;
  private Message message;
  @JsonProperty("inline_query")
  private InlineQuery inlineQuery;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public Message getMessage() {
    return message;
  }

  public void setMessage(Message message) {
    this.message = message;
  }

  public InlineQuery getInlineQuery() {
    return inlineQuery;
  }

  public void setInlineQuery(InlineQuery inlineQuery) {
    this.inlineQuery = inlineQuery;
  }
}
