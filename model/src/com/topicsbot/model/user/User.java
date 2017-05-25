package com.topicsbot.model.user;

import com.topicsbot.model.ChannelType;
import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

/**
 * Author: Artem Voronov
 */
@Entity(name="user")
@Table(name ="users")
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", unique = true, nullable = false)
  private Integer id;

  @Column(name = "external_id", unique = false, nullable = false, length = 500)
  private String externalId;

  @Column(name = "name", unique = false, nullable = false, length = 300)
  @NotNull(message = "User name is null")
  @NotEmpty(message = "User name is empty")
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
