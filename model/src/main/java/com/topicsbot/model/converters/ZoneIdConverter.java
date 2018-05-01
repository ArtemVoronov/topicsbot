package com.topicsbot.model.converters;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.time.ZoneId;

/**
 * Author: Artem Voronov
 */
@Converter
public class ZoneIdConverter implements AttributeConverter<ZoneId, String> {

  @Override
  public String convertToDatabaseColumn(ZoneId attribute) {
    return attribute.getId();
  }

  @Override
  public ZoneId convertToEntityAttribute(String dbData) {
    return ZoneId.of( dbData );
  }
}
