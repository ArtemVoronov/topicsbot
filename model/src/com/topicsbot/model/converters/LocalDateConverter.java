package com.topicsbot.model.converters;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.time.LocalDate;
import java.sql.Date;

/**
 * Author: Artem Voronov
 */
@Converter(autoApply = true)
public class LocalDateConverter implements AttributeConverter<LocalDate, Date> {

  @Override
  public Date convertToDatabaseColumn(LocalDate locDate) {
    return (locDate == null ? null : Date.valueOf(locDate));
  }

  @Override
  public LocalDate convertToEntityAttribute(Date sqlDate) {
    return (sqlDate == null ? null : sqlDate.toLocalDate());
  }
}
