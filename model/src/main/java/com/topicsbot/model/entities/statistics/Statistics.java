package com.topicsbot.model.entities.statistics;

import com.topicsbot.model.entities.chat.Chat;
import com.topicsbot.model.converters.LocalDateConverter;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * Author: Artem Voronov
 */
@MappedSuperclass
@Table(name = "statistics")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@DynamicUpdate
public abstract class Statistics {

  @OneToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "chat_id", unique = false, nullable = false)
  private Chat chat;

  @Convert(converter = LocalDateConverter.class)
  @Column(name = "create_date", unique = false, nullable = false)
  private LocalDate createDate;

  @Column(name = "flood_size", unique = false, nullable = false)
  @NotNull(message = "Flood size is null")
  private Integer floodSize = 0;

  @Column(name = "message_counter", unique = false, nullable = false)
  @NotNull(message = "Message counter is null")
  private Integer messageCounter = 0;

  @Column(name = "word_counter", unique = false, nullable = false)
  @NotNull(message = "Word counter is null")
  private Integer wordCounter = 0;

  @Column(name = "start_command_counter", unique = false, nullable = false)
  @NotNull(message = "Start command counter is null")
  private Integer startCommandCounter = 0;

  @Column(name = "help_command_counter", unique = false, nullable = false)
  @NotNull(message = "Help command counter is null")
  private Integer helpCommandCounter = 0;

  @Column(name = "topics_command_counter", unique = false, nullable = false)
  @NotNull(message = "Topics command counter is null")
  private Integer topicsCommandCounter = 0;

  @Column(name = "add_topic_command_counter", unique = false, nullable = false)
  @NotNull(message = "Add topic command counter is null")
  private Integer addTopicCommandCounter = 0;

  @Column(name = "statistics_command_counter", unique = false, nullable = false)
  @NotNull(message = "Statistics command counter is null")
  private Integer statisticsCommandCounter = 0;

  @Column(name = "settings_command_counter", unique = false, nullable = false)
  @NotNull(message = "Settings command counter is null")
  private Integer settingsCommandCounter = 0;

  @Column(name = "rate_command_counter", unique = false, nullable = false)
  @NotNull(message = "Rate command counter is null")
  private Integer rateCommandCounter = 0;

  @Column(name = "rank_command_counter", unique = false, nullable = false)
  @NotNull(message = "Rank command counter is null")
  private Integer rankCommandCounter = 0;

  @Column(name = "world_topics_command_counter", unique = false, nullable = false)
  @NotNull(message = "World topics command counter is null")
  private Integer worldTopicsCommandCounter = 0;

  @Column(name = "cancel_command_counter", unique = false, nullable = false)
  @NotNull(message = "Cancel command counter is null")
  private Integer cancelCommandCounter = 0;

  @Column(name = "donate_command_counter", unique = false, nullable = false)
  @NotNull(message = "Donate command counter is null")
  private Integer donateCommandCounter = 0;

  @Column(name = "deleted", unique = false, nullable = false)
  private boolean deleted;

  public Chat getChat() {
    return chat;
  }

  public void setChat(Chat chat) {
    this.chat = chat;
  }

  public LocalDate getCreateDate() {
    return createDate;
  }

  public void setCreateDate(LocalDate createDate) {
    this.createDate = createDate;
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

  public boolean isDeleted() {
    return deleted;
  }

  public void setDeleted(boolean deleted) {
    this.deleted = deleted;
  }

  public Integer getCancelCommandCounter() {
    return cancelCommandCounter;
  }

  public void setCancelCommandCounter(Integer cancelCommandCounter) {
    this.cancelCommandCounter = cancelCommandCounter;
  }

  public Integer getDonateCommandCounter() {
    return donateCommandCounter;
  }

  public void setDonateCommandCounter(Integer donateCommandCounter) {
    this.donateCommandCounter = donateCommandCounter;
  }

  public Integer getRankCommandCounter() {
    return rankCommandCounter;
  }

  public void setRankCommandCounter(Integer rankCommandCounter) {
    this.rankCommandCounter = rankCommandCounter;
  }
}
