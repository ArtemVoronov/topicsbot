package com.topicsbot.model.statistics;

import com.topicsbot.model.user.User;

import javax.persistence.*;

/**
 * Author: Artem Voronov
 * User statistics at the chat for whole day
 */
@Entity(name="user_day_stat")
@Table(name ="user_day_statistics")
public class UserDayStatistics extends Statistics {

  @OneToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "user_id", unique = false, nullable = false)
  private User user;
}
