package com.topicsbot.services.api.telegram.handlers;

/**
 * Author: Artem Voronov
 */
public enum UpdateType {
  //user commands
  START("start"),
  TOPICS("topics"),
  HELP("help"),
  DONATE("donate"),
  ADD("add"),
  STATISTICS("statistics"),
  RATE("rate"),
  RANK("rank"),
  WORLD_TOPICS("world_topics"),
  SETTINGS("settings"), //show keyboard 'settings'
  CANCEL("cancel"), //cancel previous action: adding topic

  //buttons
  LANGUAGE("language"), //change language (at settings keyboard)
  TIMEZONE("timezone"), //change timezone (at settings keyboard)
  LANGUAGE_KEYBOARD("language_keyboard"), //show keyboard for a language choosing
  TIMEZONE_KEYBOARD("timezone_keyboard"), //show keyboard for a timezone choosing
  CLOSE_SETTINGS("close_settings"), //hide 'settings' keyboard
  BACK_TO_SETTINGS("back_to_settings"), //show keyboard 'settings'

  //default update
  TO_STATISTICS("to_statistics"),

  //for using at unknown chats
  INLINE_QUERY("inline_query"),

  //admin commands // TODO: clean deprecated
  CLEAR("clear"),
  DELETE("delete"),
  STORE("store"),
  EXIT("exit"),
  CHATS("chats"),
  COUNT("count"),
  RECORDSTART("recordstart"),
  RECORDSTOP("recordstop"),
  ASTATISTICS("astatistics"),
  ATOPICS("atopics"),
  JVM("jvm"),
  CONF("conf"),
  SET_OPTION("set_option"),
  FREQ("freq"),
  WORLD_STAT("world_stat"),
  STOPBOT("stopbot"),
  CMD("cmd"),
  CMD_STAT("cmd_stat");

  private final String command;
  private static final String COMMAND_INIT_CHAR = "/";

  UpdateType(final String command) {
    this.command = COMMAND_INIT_CHAR + command;
  }

  public String getCommand() {
    return command;
  }

}
