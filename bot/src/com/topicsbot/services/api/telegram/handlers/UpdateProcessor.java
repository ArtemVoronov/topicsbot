package com.topicsbot.services.api.telegram.handlers;

import com.topicsbot.services.api.telegram.model.InlineQuery;
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
    try {
      UpdateType updateType = convert(update);
      if (updateType == null)
        return;

      UpdateHandler handler = handlers.get(updateType);
      if (handler == null)
        return;

      handler.handle(update);

    } catch (Exception ex) {
      logger.error(ex.getMessage(), ex);
    }
  }

  private UpdateType convert(Update update) {
    try {
      InlineQuery inlineQuery = update.getInlineQuery();
      if (inlineQuery != null)
        return UpdateType.INLINE_QUERY;

      Message message = update.getMessage();

      if (message == null)
        return null;

      String text = message.getText();
      String command = null;
      if (text == null) {
        command = UpdateType.TO_STATISTICS.getCommand();
      } else if (text.trim().startsWith(KeyboardFactory.SECRET_COMMAND_SETTINGS)) {
        command = UpdateType.SETTINGS.getCommand();
      } else if (text.trim().startsWith(KeyboardFactory.SECRET_COMMAND_CLOSE_SETTINGS)) {
        command = UpdateType.CLOSE_SETTINGS.getCommand();
      } else if (text.trim().startsWith(KeyboardFactory.SECRET_COMMAND_TIME)) {
        command = UpdateType.TIMEZONE_KEYBOARD.getCommand();
      } else if (text.trim().startsWith(KeyboardFactory.SECRET_COMMAND_LANG)) {
        command = UpdateType.LANGUAGE_KEYBOARD.getCommand();
      } else if (text.trim().startsWith(KeyboardFactory.SECRET_COMMAND_TIME_PARAM)) {
        command = UpdateType.TIMEZONE.getCommand();
      } else if (text.trim().startsWith(KeyboardFactory.SECRET_COMMAND_LANG_PARAM)) {
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
