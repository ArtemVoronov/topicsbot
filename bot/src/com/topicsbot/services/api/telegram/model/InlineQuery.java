package com.topicsbot.services.api.telegram.model;

/**
 * Author: Artem Voronov
 */
public class InlineQuery {

  private String id;
  private User from;
  private String query;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public User getFrom() {
    return from;
  }

  public void setFrom(User from) {
    this.from = from;
  }

  public String getQuery() {
    return query;
  }

  public void setQuery(String query) {
    this.query = query;
  }

  @Override
  public String toString() {
    return "InlineQuery{" +
        "id='" + id + '\'' +
        ", from=" + from +
        ", query='" + query + '\'' +
        '}';
  }
}
