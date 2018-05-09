package com.topicsbot.ui.telegram.services.communication.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Author: Artem Voronov
 */
public class ReplyKeyboardRemove {

  @JsonProperty("remove_keyboard")
  private boolean removeKeyboard;

  private boolean selective;

  public boolean isRemoveKeyboard() {
    return removeKeyboard;
  }

  public void setRemoveKeyboard(boolean removeKeyboard) {
    this.removeKeyboard = removeKeyboard;
  }

  public boolean isSelective() {
    return selective;
  }

  public void setSelective(boolean selective) {
    this.selective = selective;
  }
}
