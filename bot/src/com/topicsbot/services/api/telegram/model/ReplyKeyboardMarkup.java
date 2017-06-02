package com.topicsbot.services.api.telegram.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Author: Artem Voronov
 */
public class ReplyKeyboardMarkup {

  private List<KeyboardRow> keyboard;
  @JsonProperty("resize_keyboard")
  private boolean resizeKeyboard;
  @JsonProperty("one_time_keyboard")
  private boolean oneTimeKeyboard;
  private boolean selective;

  public List<KeyboardRow> getKeyboard() {
    return keyboard;
  }

  public void setKeyboard(List<KeyboardRow> keyboard) {
    this.keyboard = keyboard;
  }

  public boolean isResizeKeyboard() {
    return resizeKeyboard;
  }

  public void setResizeKeyboard(boolean resizeKeyboard) {
    this.resizeKeyboard = resizeKeyboard;
  }

  public boolean isOneTimeKeyboard() {
    return oneTimeKeyboard;
  }

  public void setOneTimeKeyboard(boolean oneTimeKeyboard) {
    this.oneTimeKeyboard = oneTimeKeyboard;
  }

  public boolean isSelective() {
    return selective;
  }

  public void setSelective(boolean selective) {
    this.selective = selective;
  }
}
