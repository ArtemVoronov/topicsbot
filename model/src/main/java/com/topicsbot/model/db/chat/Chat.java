package com.topicsbot.model.db.chat;

import com.topicsbot.model.converters.LocalDateConverter;
import com.topicsbot.model.converters.ZoneIdConverter;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.ZoneId;

/**
 * Author: Artem Voronov
 */
@Entity(name="chat")
@Table(name ="chats", uniqueConstraints = {@UniqueConstraint(columnNames = {"external_id", "channel"})})
@DynamicUpdate
public class Chat {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", unique = true, nullable = false)
  private Integer id;

  @Column(name = "external_id", unique = false, nullable = false, length = 500)
  private String externalId;

  @Column(name = "title", unique = false, nullable = true, length = 400)
  private String title;

  @Enumerated(EnumType.STRING)
  @Column(name="channel", unique = false, nullable = false)
  @NotNull(message = "Channel type is null")
  private ChannelType channel;

  @Enumerated(EnumType.STRING)
  @Column(name="type", unique = false, nullable = false)
  @NotNull(message = "Chat type is null")
  private ChatType type;

  @Enumerated(EnumType.STRING)
  @Column(name="language", unique = false, nullable = false)
  @NotNull(message = "Chat language is null")
  private ChatLanguage language;

  @Column(name = "size", unique = false, nullable = false)
  @NotNull(message = "Chat size is null")
  private Integer size;

  @Convert(converter = ZoneIdConverter.class)
  @Column(name = "timezone", unique = false, nullable = false)
  private ZoneId timezone;

  @Convert(converter = LocalDateConverter.class)
  @Column(name = "rebirth_date", unique = false, nullable = false)
  private LocalDate rebirthDate;

  @Column(name = "average_flood", unique = false, nullable = false)
  @NotNull(message = "Average flood is null")
  private Double averageFlood = 0.0;

  public Integer getId() {
    return id;
  }

  public String getExternalId() {
    return externalId;
  }

  public void setExternalId(String externalId) {
    this.externalId = externalId;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public ChannelType getChannel() {
    return channel;
  }

  public void setChannel(ChannelType channel) {
    this.channel = channel;
  }

  public ChatType getType() {
    return type;
  }

  public void setType(ChatType type) {
    this.type = type;
  }

  public ChatLanguage getLanguage() {
    return language;
  }

  public void setLanguage(ChatLanguage language) {
    this.language = language;
  }

  public Integer getSize() {
    return size;
  }

  public void setSize(Integer size) {
    this.size = size;
  }

  public ZoneId getTimezone() {
    return timezone;
  }

  public void setTimezone(ZoneId timezone) {
    this.timezone = timezone;
    this.rebirthDate = LocalDate.now(timezone);
  }

  public LocalDate getRebirthDate() {
    return rebirthDate;
  }

  public void setRebirthDate(LocalDate rebirthDate) {
    this.rebirthDate = rebirthDate;
  }

  public Double getAverageFlood() {
    return averageFlood;
  }

  public void setAverageFlood(Double averageFlood) {
    this.averageFlood = averageFlood;
  }

  @Transient
  public String getLanguageShort() {
    return language.name().toLowerCase();
  }
}
