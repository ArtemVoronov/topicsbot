package com.topicsbot.services.api.telegram.handlers;

import com.topicsbot.services.api.telegram.model.Message;
import com.topicsbot.services.api.telegram.model.Update;
import com.topicsbot.services.cache.CacheService;
import org.apache.log4j.Logger;

import java.util.Map;

/**
 * author: Artem Voronov
 */
public class UpdateProcessor {
  private static final Logger logger = Logger.getLogger("TELEGRAM_API_SERVICE");
  private final Map<UpdateType, UpdateHandler> handlers;
  private final String botUserName;
  private final CacheService cacheService;

  public UpdateProcessor(String botUserName, Map<UpdateType, UpdateHandler> handlers, CacheService cacheService) {
    this.botUserName = botUserName.toUpperCase();
    this.handlers = handlers;
    this.cacheService = cacheService;
  }

  public void process(Update update) {
    UpdateType updateType = convert(update);
    try {
      UpdateHandler handler = handlers.get(updateType);
      if (handler == null)
        throw new IllegalArgumentException("Missed handler for update type: " + updateType);

      handler.handle(update);

    } catch (Exception ex) {
      logger.error(ex.getMessage(), ex);
    }
  }

  //TODO: move to somewhere
  static final char HIDDEN_CHAR = '\u2063';
  static final String SECRET_COMMAND_LANG_PARAM = HIDDEN_CHAR + "";
  static final String SECRET_COMMAND_TIME_PARAM = HIDDEN_CHAR + " " + SECRET_COMMAND_LANG_PARAM;
  static final String SECRET_COMMAND_LANG = HIDDEN_CHAR + " " + SECRET_COMMAND_TIME_PARAM;
  static final String SECRET_COMMAND_TIME = HIDDEN_CHAR + " " + SECRET_COMMAND_LANG;
  static final String SECRET_COMMAND_CLOSE_SETTINGS = HIDDEN_CHAR + " " + SECRET_COMMAND_TIME;
  static final String SECRET_COMMAND_SETTINGS = HIDDEN_CHAR + " " + SECRET_COMMAND_CLOSE_SETTINGS;

  private UpdateType convert(Update update) {
    try {
      Message message = update.getMessage();
      String text = message.getText();
      String command = null;
      if (text == null) {
        command = UpdateType.TO_STATISTICS.getCommand();
      } else if (text.trim().startsWith(SECRET_COMMAND_SETTINGS)) {
        command = UpdateType.SETTINGS.getCommand();
      } else if (text.trim().startsWith(SECRET_COMMAND_CLOSE_SETTINGS)) {
        command = UpdateType.CLOSE_SETTINGS.getCommand();
      } else if (text.trim().startsWith(SECRET_COMMAND_TIME)) {
        command = UpdateType.TIMEZONE_KEYBOARD.getCommand();
      } else if (text.trim().startsWith(SECRET_COMMAND_LANG)) {
        command = UpdateType.LANGUAGE_KEYBOARD.getCommand();
      } else if (text.trim().startsWith(SECRET_COMMAND_TIME_PARAM)) {
        command = UpdateType.TIMEZONE.getCommand();
      } else if (text.trim().startsWith(SECRET_COMMAND_LANG_PARAM)) {
        command = UpdateType.LANGUAGE.getCommand();
      } else {
        text = text.trim();
        if (text.startsWith("/")) {
          command = text.split(" ")[0];
          cleanTopicsAdding(update);
        } else {
          command = isTopicsAdding(update) ? UpdateType.ADD.getCommand() : UpdateType.TO_STATISTICS.getCommand();
        }
      }
      command = command.toUpperCase().replace("/", "").replace(botUserName, "");
      return UpdateType.valueOf(command);


    } catch (Exception ex) {
      logger.error(ex.getMessage(), ex);
      return null;
    }
  }

  private boolean isTopicsAdding(Update update) {
    Message message = update.getMessage();
    return message != null && cacheService.hasWaiter(message.getChatId(), message.getUserId());

  }

  private void cleanTopicsAdding(Update update) {
    Message message = update.getMessage();

    if (message == null)
      return;

    cacheService.removeWaiter(message.getChatId(), message.getUserId());
  }
}
