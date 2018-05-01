package com.topicsbot.model.db.user;

import com.topicsbot.model.db.chat.ChannelType;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;


/**
 * Author: Artem Voronov
 */
@Entity(name="user")
@Table(name ="users", uniqueConstraints = {@UniqueConstraint(columnNames = {"external_id", "channel"})})
@DynamicUpdate
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", unique = true, nullable = false)
  private Integer id;

  @Column(name = "external_id", unique = false, nullable = false, length = 500)
  private String externalId;

  @Column(name = "name", unique = false, nullable = false, length = 300)
  @NotBlank(message = "User name is blank")
  private String name;

  @Enumerated(EnumType.STRING)
  @Column(name="channel", unique = false, nullable = false)
  @NotNull(message = "User channel is null")
  private ChannelType channel;

  public Integer getId() {
    return id;
  }

  public String getExternalId() {
    return externalId;
  }

  public void setExternalId(String externalId) {
    this.externalId = externalId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public ChannelType getChannel() {
    return channel;
  }

  public void setChannel(ChannelType channel) {
    this.channel = channel;
  }
}
