package com.topicsbot.utils;

/**
 * Author: Artem Voronov
 */
public class MessageParser {

//  public static String extractCommandStr(String text) {//TODO: clean
//    return text.startsWith("/") ? text.split(" ")[0] : Commands.TO_STATISTICS.getCommand();
//  }
//
//  public static String extractParameter(String text, int paramIndex) {
//    if (paramIndex < 1) {
//      return "";
//    }
//
//    String command = extractCommandStr(text);
//    if (command.equals("")) {
//      return "";
//    } else {
//      String parameters = text.replaceFirst(command, "").trim();//text.split(command);
//      if (!parameters.isEmpty()) {
//        try {
//          String[] paramTokens = parameters.split(" ");
//          return paramTokens.length >= paramIndex ? paramTokens[--paramIndex].trim() : "";
//        } catch (Exception ex) {
//          logger.error("extractParameter error", ex);
//          return "";
//        }
//      } else {
//        return "";
//      }
//    }
//  }
}
