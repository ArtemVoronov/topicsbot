package com.topicsbot.services.api.telegram.model;

/**
 * Author: Artem Voronov
 */
public class Converter {

  public static com.topicsbot.model.chat.ChatType convert(ChatType chatType) {
    switch (chatType) {
      case PRIVATE:
        return com.topicsbot.model.chat.ChatType.PRIVATE;
      case GROUP:
        return com.topicsbot.model.chat.ChatType.GROUP;
      case SUPERGROUP:
        return com.topicsbot.model.chat.ChatType.SUPER_GROUP;
      case CHANNEL:
        return com.topicsbot.model.chat.ChatType.CHANNEL;
      default:
        throw new IllegalArgumentException("unkown chat type: " + chatType);
    }
  }
}
