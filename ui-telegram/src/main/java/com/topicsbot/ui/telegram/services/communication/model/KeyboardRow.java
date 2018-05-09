package com.topicsbot.ui.telegram.services.communication.model;

import java.util.ArrayList;

/**
 * Author: Artem Voronov
 */
public class KeyboardRow extends ArrayList<KeyboardButton> {

  public KeyboardRow() {
  }

  public boolean add(String text) {
    return super.add(new KeyboardButton(text));
  }

  public void add(int index, String text) {
    super.add(index, new KeyboardButton(text));
  }

  public boolean contains(String text) {
    return super.contains(new KeyboardButton(text));
  }

  public int lastIndexOf(String text) {
    return super.lastIndexOf(new KeyboardButton(text));
  }

  public int indexOf(String text) {
    return super.indexOf(new KeyboardButton(text));
  }

  public KeyboardButton set(int index, String text) {
    return (KeyboardButton)super.set(index, new KeyboardButton(text));
  }

  public boolean remove(String text) {
    return super.remove(new KeyboardButton(text));
  }
}
