package com.topicsbot.services.api.telegram.handlers.admin;

import com.topicsbot.services.api.telegram.TelegramApiProvider;
import com.topicsbot.services.api.telegram.model.Message;
import com.topicsbot.services.cache.CacheService;

/**
 * Author: Artem Voronov
 */
public class CommonAdminHandler {

  private static final int ADMIN = 193062503;

  protected final TelegramApiProvider telegramApiProvider;
  protected final CacheService cache;

  public CommonAdminHandler(TelegramApiProvider telegramApiProvider, CacheService cache) {
    this.telegramApiProvider = telegramApiProvider;
    this.cache = cache;
  }

  protected boolean isFromAdmin(Message message) {
    return ADMIN == message.getFrom().getId();
  }
}
