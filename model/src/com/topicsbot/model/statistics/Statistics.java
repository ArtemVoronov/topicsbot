package com.topicsbot.model.statistics;

import com.topicsbot.model.chat.Chat;
import com.topicsbot.model.converters.LocalDateConverter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * Author: Artem Voronov
 */
@Entity(name = "stat")
@Table(name = "statistics")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class Statistics {

  @Id
  @GeneratedValue(strategy = GenerationType.TABLE)
  @Column(name = "id", unique = true, nullable = false)
  private Integer id;

  @OneToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "chat_id", unique = false, nullable = false)
  private Chat chat;

  @Convert(converter = LocalDateConverter.class)
  @Column(name = "date", unique = false, nullable = true)
  private LocalDate date;

  @Column(name = "flood_size", unique = false, nullable = false)
  @NotNull(message = "Flood size is null")
  private Integer floodSize;

  @Column(name = "message_counter", unique = false, nullable = false)
  @NotNull(message = "Message counter is null")
  private Integer messageCounter;

  @Column(name = "word_counter", unique = false, nullable = false)
  @NotNull(message = "Word counter is null")
  private Integer wordCounter;

  @Column(name = "start_command_counter", unique = false, nullable = false)
  @NotNull(message = "Start command counter is null")
  private Integer startCommandCounter;

  @Column(name = "help_command_counter", unique = false, nullable = false)
  @NotNull(message = "Help command counter is null")
  private Integer helpCommandCounter;

  @Column(name = "topics_command_counter", unique = false, nullable = false)
  @NotNull(message = "Topics command counter is null")
  private Integer topicsCommandCounter;

  @Column(name = "add_topic_command_counter", unique = false, nullable = false)
  @NotNull(message = "Add topic command counter is null")
  private Integer addTopicCommandCounter;

  @Column(name = "statistics_command_counter", unique = false, nullable = false)
  @NotNull(message = "Statistics command counter is null")
  private Integer statisticsCommandCounter;

  @Column(name = "settings_command_counter", unique = false, nullable = false)
  @NotNull(message = "Settings command counter is null")
  private Integer settingsCommandCounter;

  @Column(name = "rate_command_counter", unique = false, nullable = false)
  @NotNull(message = "Rate command counter is null")
  private Integer rateCommandCounter;

  @Column(name = "world_topics_command_counter", unique = false, nullable = false)
  @NotNull(message = "World topics command counter is null")
  private Integer worldTopicsCommandCounter;

  public Integer getId() {
    return id;
  }

  public Chat getChat() {
    return chat;
  }

  public void setChat(Chat chat) {
    this.chat = chat;
  }

  public LocalDate getDate() {
    return date;
  }

  public void setDate(LocalDate date) {
    this.date = date;
  }

  public Integer getFloodSize() {
    return floodSize;
  }

  public void setFloodSize(Integer floodSize) {
    this.floodSize = floodSize;
  }

  public Integer getMessageCounter() {
    return messageCounter;
  }

  public void setMessageCounter(Integer messageCounter) {
    this.messageCounter = messageCounter;
  }

  public Integer getWordCounter() {
    return wordCounter;
  }

  public void setWordCounter(Integer wordCounter) {
    this.wordCounter = wordCounter;
  }

  public Integer getStartCommandCounter() {
    return startCommandCounter;
  }

  public void setStartCommandCounter(Integer startCommandCounter) {
    this.startCommandCounter = startCommandCounter;
  }

  public Integer getHelpCommandCounter() {
    return helpCommandCounter;
  }

  public void setHelpCommandCounter(Integer helpCommandCounter) {
    this.helpCommandCounter = helpCommandCounter;
  }

  public Integer getTopicsCommandCounter() {
    return topicsCommandCounter;
  }

  public void setTopicsCommandCounter(Integer topicsCommandCounter) {
    this.topicsCommandCounter = topicsCommandCounter;
  }

  public Integer getAddTopicCommandCounter() {
    return addTopicCommandCounter;
  }

  public void setAddTopicCommandCounter(Integer addTopicCommandCounter) {
    this.addTopicCommandCounter = addTopicCommandCounter;
  }

  public Integer getStatisticsCommandCounter() {
    return statisticsCommandCounter;
  }

  public void setStatisticsCommandCounter(Integer statisticsCommandCounter) {
    this.statisticsCommandCounter = statisticsCommandCounter;
  }

  public Integer getSettingsCommandCounter() {
    return settingsCommandCounter;
  }

  public void setSettingsCommandCounter(Integer settingsCommandCounter) {
    this.settingsCommandCounter = settingsCommandCounter;
  }

  public Integer getRateCommandCounter() {
    return rateCommandCounter;
  }

  public void setRateCommandCounter(Integer rateCommandCounter) {
    this.rateCommandCounter = rateCommandCounter;
  }

  public Integer getWorldTopicsCommandCounter() {
    return worldTopicsCommandCounter;
  }

  public void setWorldTopicsCommandCounter(Integer worldTopicsCommandCounter) {
    this.worldTopicsCommandCounter = worldTopicsCommandCounter;
  }
}
