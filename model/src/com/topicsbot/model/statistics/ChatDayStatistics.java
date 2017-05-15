package com.topicsbot.model.statistics;


import javax.persistence.*;

/**
 * Author: Artem Voronov
 * Chat statistics for whole day
 */
@Entity(name="chat_day_stat")
@Table(name ="chat_day_statistics")
public class ChatDayStatistics extends Statistics {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", unique = true, nullable = false)
  private Integer id;

  public Integer getId() {
    return id;
  }
}
