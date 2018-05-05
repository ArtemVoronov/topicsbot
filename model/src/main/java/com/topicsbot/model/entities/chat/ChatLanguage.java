package com.topicsbot.model.entities.chat;

import java.util.HashMap;
import java.util.Map;

/**
 * Author: Artem Voronov
 */
public enum ChatLanguage {
  RU("Русский"), EN("English"), PT("Português"), ES("Español"), DE("Deutsch"), FR("Français"), IT("Italiano"),
  PL("Polski"), JA("日本語"), ZH("中文"), AR("العربية"), CS("Čeština"), HI("हिन्दी"), TR("Türkçe"), KO("한국어");


  public static final Map<String, ChatLanguage> mappingByName = new HashMap<>();
  static {
    for (ChatLanguage lang : ChatLanguage.values()) {
      mappingByName.put(lang.getName(), lang);
    }
  }

  public final String name;

  ChatLanguage(final String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }
}
