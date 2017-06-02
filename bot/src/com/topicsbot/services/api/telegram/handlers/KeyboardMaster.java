package com.topicsbot.services.api.telegram.handlers;

/**
 * Author: Artem Voronov
 */
public class KeyboardMaster {
  //TODO
  private static final char HIDDEN_CHAR = '\u2063';
  public static final String SECRET_COMMAND_LANG_PARAM = HIDDEN_CHAR + "";
  public static final String SECRET_COMMAND_TIME_PARAM = HIDDEN_CHAR + " " + SECRET_COMMAND_LANG_PARAM;
  public static final String SECRET_COMMAND_LANG = HIDDEN_CHAR + " " + SECRET_COMMAND_TIME_PARAM;
  public static final String SECRET_COMMAND_TIME = HIDDEN_CHAR + " " + SECRET_COMMAND_LANG;
  public static final String SECRET_COMMAND_CLOSE_SETTINGS = HIDDEN_CHAR + " " + SECRET_COMMAND_TIME;
  public static final String SECRET_COMMAND_SETTINGS = HIDDEN_CHAR + " " + SECRET_COMMAND_CLOSE_SETTINGS;
}
