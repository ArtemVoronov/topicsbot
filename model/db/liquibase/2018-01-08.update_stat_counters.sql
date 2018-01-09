-- liquibase formatted sql
-- changeset voronov:1

alter table chat_day_statistics add column rank_command_counter integer not null DEFAULT 0;

alter table user_day_statistics add column rank_command_counter integer not null DEFAULT 0;


-- rollback alter table user_day_statistics drop column rank_command_counter

-- rollback alter table chat_day_statistics drop column rank_command_counter;