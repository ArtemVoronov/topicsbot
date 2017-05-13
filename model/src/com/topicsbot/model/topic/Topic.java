package com.topicsbot.model.topic;

import com.topicsbot.model.chat.Chat;
import com.topicsbot.model.converters.LocalDateConverter;
import com.topicsbot.model.user.User;

import javax.persistence.*;
import java.time.LocalDate;

/**
 * Author: Artem Voronov
 */
@Entity(name="topic")
@Table(name ="topics")
public class Topic {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", unique = true, nullable = false)
  private Integer id;

  @Column(name = "text", unique = false, nullable = true, length = 400)
  private String text;

  @OneToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "chat_id", unique = false, nullable = false)
  private Chat chat;

  @OneToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "author_id", unique = false, nullable = false)
  private User author;

  @Convert(converter = LocalDateConverter.class)
  @Column(name = "create_date", unique = false, nullable = false)
  private LocalDate createDate;

  public Integer getId() {
    return id;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public Chat getChat() {
    return chat;
  }

  public void setChat(Chat chat) {
    this.chat = chat;
  }

  public User getAuthor() {
    return author;
  }

  public void setAuthor(User author) {
    this.author = author;
  }

  public LocalDate getCreateDate() {
    return createDate;
  }

  public void setCreateDate(LocalDate createDate) {
    this.createDate = createDate;
  }
}
