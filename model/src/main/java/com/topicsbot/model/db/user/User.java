package com.topicsbot.model.db.user;

import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;

@Entity(name = "user")
@Table(name ="users")
@DynamicUpdate
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", unique = true, nullable = false)
  private Integer id;

  @Column(name = "name", unique = false, nullable = false, length = 200)
  private String name;

  public Integer getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
