-- liquibase formatted sql
-- changeset voronov:1

  CREATE DATABASE IF NOT EXISTS topicsbotdb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;


  create table chat_day_statistics (
    id integer not null auto_increment,
    add_topic_command_counter integer not null,
    cancel_command_counter integer not null,
    create_date date not null,
    deleted bit not null,
    donate_command_counter integer not null,
    flood_size integer not null,
    help_command_counter integer not null,
    message_counter integer not null,
    rank_command_counter integer not null,
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
    average_flood double precision not null,
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

  create table config_params (
    id integer not null auto_increment,
    param_name varchar(255) not null unique,
    param_value varchar(255) not null,
    primary key (id)
  ) ENGINE=InnoDB default CHARSET=utf8;

  create table topics (
    id integer not null auto_increment,
    create_date date not null,
    deleted bit not null,
    text varchar(400),
    author_id integer not null,
    chat_id integer not null,
    primary key (id)
  ) ENGINE=InnoDB default CHARSET=utf8;

  create table user_day_statistics (
    id integer not null auto_increment,
    add_topic_command_counter integer not null,
    cancel_command_counter integer not null,
    create_date date not null,
    deleted bit not null,
    donate_command_counter integer not null,
    flood_size integer not null,
    help_command_counter integer not null,
    message_counter integer not null,
    rank_command_counter integer not null,
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

  alter table chats
    add constraint fk_chats_composite_key
    unique (external_id, channel);

  alter table users
    add constraint fk_users_composite_key
    unique (external_id, channel);

  alter table chat_day_statistics
    add constraint fk_chat_day_stats_to_chats
    foreign key (chat_id)
    references chats (id);

  alter table topics
    add constraint fk_topics_to_users
    foreign key (author_id)
    references users (id);

  alter table topics
    add constraint fk_topics_to_chats
    foreign key (chat_id)
    references chats (id);

  alter table user_day_statistics
    add constraint fk_user_day_stats_to_chats
    foreign key (chat_id)
    references chats (id);

  alter table user_day_statistics
    add constraint fk_user_day_stats_to_users
    foreign key (user_id)
    references users (id);


  insert into config_params(param_name, param_value) values('telegram.bot.token', '123');
  insert into config_params(param_name, param_value) values('entities.results.batch.size', '100');
  insert into config_params(param_name, param_value) values('entities.results.fetch.size', 'INT_MIN');

-- rollback ALTER TABLE user_day_statistics             DROP FOREIGN KEY fk_user_day_stats_to_users
-- rollback ALTER TABLE user_day_statistics             DROP FOREIGN KEY fk_user_day_stats_to_chats
-- rollback ALTER TABLE topics                          DROP FOREIGN KEY fk_topics_to_chats
-- rollback ALTER TABLE topics                          DROP FOREIGN KEY fk_topics_to_users
-- rollback ALTER TABLE chat_day_statistics             DROP FOREIGN KEY fk_chat_day_stats_to_chats
-- rollback ALTER TABLE users                           DROP FOREIGN KEY fk_users_composite_key
-- rollback ALTER TABLE chats                           DROP FOREIGN KEY fk_chats_composite_key


-- rollback DROP TABLE users;
-- rollback DROP TABLE user_day_statistics;
-- rollback DROP TABLE topics;
-- rollback DROP TABLE config_params;
-- rollback DROP TABLE chats;
-- rollback DROP TABLE chat_day_statistics;