package com.topicsbot.ui.telegram.services.communication.model;

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
