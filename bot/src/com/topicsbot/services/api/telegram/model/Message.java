package com.topicsbot.services.api.telegram.model;

/**
 * Author: Artem Voronov
 */
public class Message {
  private int id;
  private User from;
  private long date;
  private Chat chat;
  private String text;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public User getFrom() {
    return from;
  }

  public void setFrom(User from) {
    this.from = from;
  }

  public long getDate() {
    return date;
  }

  public void setDate(long date) {
    this.date = date;
  }

  public Chat getChat() {
    return chat;
  }

  public void setChat(Chat chat) {
    this.chat = chat;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public String getChatId() {
    return Long.toString(chat.getId());
  }

  public String getUserId() {
    return Integer.toString(from.getId());
  }

  public String getUserName() {
    StringBuilder sb = new StringBuilder();
    String firstName = from.getFirstName();
    String lastName = from.getLastName();

    if (from.getFirstName() != null) {
      sb.append(firstName);
    }

    if (lastName != null) {
      sb.append(" ").append(lastName);
    }

    return sb.toString();
  }
}
