package com.topicsbot.services.api.telegram.model;

/**
 * Author: Artem Voronov
 */
public class KeyboardButton {

  public KeyboardButton() {
  }

  public KeyboardButton(String text) {
    this.text = text;
  }

  private String text;

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }
}
