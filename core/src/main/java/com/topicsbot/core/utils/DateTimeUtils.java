package com.topicsbot.core.utils;

import java.time.*;

/**
 * author: Artem Voronov
 */
public class DateTimeUtils {

  public static Clock localDateTime2Clock(LocalDateTime localDateTime) {
    Instant instant = localDateTime.atZone(ZoneId.systemDefault()).toInstant();
    return Clock.fixed(instant, ZoneId.systemDefault());
  }
}
