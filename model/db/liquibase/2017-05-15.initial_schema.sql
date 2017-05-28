-- liquibase formatted sql
-- changeset voronov:1

create schema if not exists topics_bot
  character set utf8mb4
  collate utf8mb4_unicode_ci;

create table chat_day_statistics (
  id integer not null auto_increment,
  add_topic_command_counter integer not null,
  date date,
  flood_size integer not null,
  help_command_counter integer not null,
  message_counter integer not null,
  rate_command_counter integer not null,
  settings_command_counter integer not null,
  start_command_counter integer not null,
  statistics_command_counter integer not null,
  topics_command_counter integer not null,
  word_counter integer not null,
  world_topics_command_counter integer not null,
  chat_id integer not null,
  primary key (id)
) ENGINE=InnoDB default CHARSET=utf8;

create table chats (
  id integer not null auto_increment,
  channel varchar(255) not null,
  external_id varchar(500) not null,
  language varchar(255) not null,
  rebirth_date date not null,
  size integer not null,
  timezone varchar(255) not null,
  title varchar(400),
  type varchar(255) not null,
  primary key (id)
) ENGINE=InnoDB default CHARSET=utf8;

create table topics (
  id integer not null auto_increment,
  create_date date not null,
  text varchar(400),
  author_id integer not null,
  chat_id integer not null,
  primary key (id)
) ENGINE=InnoDB default CHARSET=utf8;

create table user_day_statistics (
  id integer not null auto_increment,
  add_topic_command_counter integer not null,
  date date,
  flood_size integer not null,
  help_command_counter integer not null,
  message_counter integer not null,
  rate_command_counter integer not null,
  settings_command_counter integer not null,
  start_command_counter integer not null,
  statistics_command_counter integer not null,
  topics_command_counter integer not null,
  word_counter integer not null,
  world_topics_command_counter integer not null,
  chat_id integer not null,
  user_id integer not null,
  primary key (id)
) ENGINE=InnoDB default CHARSET=utf8;

create table users (
  id integer not null auto_increment,
  channel varchar(255) not null,
  external_id varchar(500) not null,
  name varchar(300) not null,
  primary key (id)
) ENGINE=InnoDB default CHARSET=utf8;

alter table chats add constraint UK_9qo83j2wg125byyn6mw77i305  unique (external_id, channel);

alter table users add constraint UK_r3i1mr7suidhs19p9d3yuf77n  unique (external_id, channel);

alter table chat_day_statistics
  add constraint FK_s8u97llr77noj08ellgixo2x1
foreign key (chat_id)
references chats (id);

alter table topics
  add constraint FK_hjticaks4ayit8nwchseme2f5
foreign key (author_id)
references users (id);

alter table topics
  add constraint FK_kixu2lxrfuvbblnd3ibk7qlq4
foreign key (chat_id)
references chats (id);

alter table user_day_statistics
  add constraint FK_f4jdn1tc4lc4466d8aqislrm4
foreign key (chat_id)
references chats (id);

alter table user_day_statistics
  add constraint FK_1u5wm6kcyf0kep1gcua5mipk8
foreign key (user_id)
references users (id);

-- rollback ALTER TABLE user_day_statistics DROP FOREIGN KEY FK_1u5wm6kcyf0kep1gcua5mipk8;
-- rollback ALTER TABLE user_day_statistics DROP FOREIGN KEY FK_f4jdn1tc4lc4466d8aqislrm4;
-- rollback ALTER TABLE topics DROP FOREIGN KEY FK_kixu2lxrfuvbblnd3ibk7qlq4;
-- rollback ALTER TABLE topics DROP FOREIGN KEY FK_hjticaks4ayit8nwchseme2f5;
-- rollback ALTER TABLE chat_day_statistics DROP FOREIGN KEY FK_s8u97llr77noj08ellgixo2x1;

-- rollback ALTER TABLE users DROP INDEX UK_r3i1mr7suidhs19p9d3yuf77n;
-- rollback ALTER TABLE chats DROP INDEX UK_9qo83j2wg125byyn6mw77i305;

-- rollback DROP TABLE users;
-- rollback DROP TABLE user_day_statistics;
-- rollback DROP TABLE topics;
-- rollback DROP TABLE chats;
-- rollback DROP TABLE chat_day_statistics;