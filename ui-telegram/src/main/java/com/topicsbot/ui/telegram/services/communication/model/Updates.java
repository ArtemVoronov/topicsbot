package com.topicsbot.ui.telegram.services.communication.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Author: Artem Voronov
 */
public class Updates {

  @JsonProperty("result")
  private List<Update> updates;

  public List<Update> getUpdates() {
    return updates;
  }

  public void setUpdates(List<Update> updates) {
    this.updates = updates;
  }

  public boolean isEmpty() {
    return updates == null || updates.isEmpty();
  }

  public int getLastUpdateId() {
    return updates.get(updates.size() - 1).getId();
  }
}
