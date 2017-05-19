package com.topicsbot.services.api.telegram.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Author: Artem Voronov
 */
public class Chat {
  private long id;
  @JsonProperty("type")
  private ChatType type;
  private String title;
  @JsonProperty("first_name")
  private String firstName;
  @JsonProperty("last_name")
  private String lastName;
  @JsonProperty("username")
  private String userName;
  @JsonProperty("all_members_are_administrators")
  private boolean onlyAdmins;

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public ChatType getType() {
    return type;
  }

  public void setType(ChatType type) {
    this.type = type;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public boolean isOnlyAdmins() {
    return onlyAdmins;
  }

  public void setOnlyAdmins(boolean onlyAdmins) {
    this.onlyAdmins = onlyAdmins;
  }
}
