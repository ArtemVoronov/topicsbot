package com.topicsbot.model.entities.chat;

import java.util.HashMap;
import java.util.Map;

/**
 * Author: Artem Voronov
 */
public enum TimeZones {
  GMT_MINUS_13("Etc/GMT-13", "UTC+13:00"),
  GMT_MINUS_12("Etc/GMT-12", "UTC+12:00"), GMT_MINUS_11("Etc/GMT-11", "UTC+11:00"), GMT_MINUS_10("Etc/GMT-10", "UTC+10:00"),
  GMT_MINUS_9("Etc/GMT-9", "UTC+09:00"), GMT_MINUS_8("Etc/GMT-8", "UTC+08:00"), GMT_MINUS_7("Etc/GMT-7", "UTC+07:00"),
  GMT_MINUS_6("Etc/GMT-6", "UTC+06:00"), GMT_MINUS_5("Etc/GMT-5", "UTC+05:00"), GMT_MINUS_4("Etc/GMT-4", "UTC+04:00"),
  GMT_MINUS_3("Etc/GMT-3", "UTC+03:00"), GMT_MINUS_2("Etc/GMT-2", "UTC+02:00"),GMT_MINUS_1("Etc/GMT-1", "UTC+01:00"),
  GMT_0("Etc/GMT0", "UTC"),
  GMT_PLUS_1("Etc/GMT+1", "UTC-01:00"), GMT_PLUS_2("Etc/GMT+2", "UTC-02:00"), GMT_PLUS_3("Etc/GMT+3", "UTC-03:00"),
  GMT_PLUS_4("Etc/GMT+4", "UTC-04:00"), GMT_PLUS_5("Etc/GMT+5", "UTC-05:00"), GMT_PLUS_6("Etc/GMT+6", "UTC-06:00"),
  GMT_PLUS_7("Etc/GMT+7", "UTC-07:00"), GMT_PLUS_8("Etc/GMT+8", "UTC-08:00"), GMT_PLUS_9("Etc/GMT+9", "UTC-09:00"),
  GMT_PLUS_10("Etc/GMT+10", "UTC-10:00"), GMT_PLUS_11("Etc/GMT+11", "UTC-11:00"), GMT_PLUS_12("Etc/GMT+12", "UTC-12:00");

  private final String name;
  private final String label;

  public static final Map<String, String> mappingTo = new HashMap<>();
  public static final Map<String, String> mappingFrom = new HashMap<>();
  static {
    mappingTo.put("UTC-12:00", "Etc/GMT+12");
    mappingTo.put("UTC-11:00", "Etc/GMT+11");
    mappingTo.put("UTC-10:00", "Etc/GMT+10");
    mappingTo.put("UTC-09:00", "Etc/GMT+9");
    mappingTo.put("UTC-08:00", "Etc/GMT+8");
    mappingTo.put("UTC-07:00", "Etc/GMT+7");
    mappingTo.put("UTC-06:00", "Etc/GMT+6");
    mappingTo.put("UTC-05:00", "Etc/GMT+5");
    mappingTo.put("UTC-04:00", "Etc/GMT+4");
    mappingTo.put("UTC-03:00", "Etc/GMT+3");
    mappingTo.put("UTC-02:00", "Etc/GMT+2");
    mappingTo.put("UTC-01:00", "Etc/GMT+1");
    mappingTo.put("UTC", "Etc/GMT0");
    mappingTo.put("UTC+01:00", "Etc/GMT-1");
    mappingTo.put("UTC+02:00", "Etc/GMT-2");
    mappingTo.put("UTC+03:00", "Etc/GMT-3");
    mappingTo.put("UTC+04:00", "Etc/GMT-4");
    mappingTo.put("UTC+05:00", "Etc/GMT-5");
    mappingTo.put("UTC+06:00", "Etc/GMT-6");
    mappingTo.put("UTC+07:00", "Etc/GMT-7");
    mappingTo.put("UTC+08:00", "Etc/GMT-8");
    mappingTo.put("UTC+09:00", "Etc/GMT-9");
    mappingTo.put("UTC+10:00", "Etc/GMT-10");
    mappingTo.put("UTC+11:00", "Etc/GMT-11");
    mappingTo.put("UTC+12:00", "Etc/GMT-12");
    mappingTo.put("UTC+13:00", "Etc/GMT-13");

    mappingTo.entrySet().stream().forEach(entry -> {
      mappingFrom.put(entry.getValue(), entry.getKey());
    });

  }

  TimeZones(final String name, final String label) {
    this.name = name;
    this.label = label;
  }

  public String getName() {
    return name;
  }

  public String getLabel() {
    return label;
  }

  @Override
  public String toString() {
    return label;
  }
}
