package com.topicsbot.model.entities.statistics;


import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;

/**
 * Author: Artem Voronov
 * Chat statistics for the whole day
 */
@Entity(name="chat_day_stat")
@Table(name ="chat_day_statistics")
@DynamicUpdate
public class ChatDayStatistics extends Statistics {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", unique = true, nullable = false)
  private Integer id;

  public Integer getId() {
    return id;
  }
}
