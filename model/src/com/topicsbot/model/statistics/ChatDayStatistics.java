package com.topicsbot.model.statistics;


import javax.persistence.*;

/**
 * Author: Artem Voronov
 * Chat statistics for whole day
 */
@Entity(name="chat_day_stat")
@Table(name ="chat_day_statistics")
public class ChatDayStatistics extends Statistics {

}
