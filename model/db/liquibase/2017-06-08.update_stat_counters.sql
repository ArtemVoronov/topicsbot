-- liquibase formatted sql
-- changeset voronov:1

alter table chat_day_statistics add column cancel_command_counter integer not null DEFAULT 0;

alter table chat_day_statistics add column donate_command_counter integer not null DEFAULT 0;

alter table user_day_statistics add column cancel_command_counter integer not null DEFAULT 0;

alter table user_day_statistics add column donate_command_counter integer not null DEFAULT 0;

-- rollback alter table user_day_statistics drop column donate_command_counter;
-- rollback alter table user_day_statistics drop column cancel_command_counter

-- rollback alter table chat_day_statistics drop column donate_command_counter;
-- rollback alter table chat_day_statistics drop column cancel_command_counter;