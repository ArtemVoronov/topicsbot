package com.topicsbot.model.services.config

/**
 * Author: Artem Voronov
 */
enum TestConfigParams {
  TELEGRAM_BOT_TOKEN("telegram.bot.token", "test_telegram_bot_token")

  final String key
  final String value

  TestConfigParams(final String key, final String value) {
    this.key = key
    this.value = value
  }

  String getKey() {
    return key
  }

  String getValue() {
    return value
  }
}