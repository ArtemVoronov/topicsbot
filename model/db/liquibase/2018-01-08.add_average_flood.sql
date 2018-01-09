-- liquibase formatted sql
-- changeset voronov:1

alter table chats add column average_flood double precision not null DEFAULT 0;

-- rollback alter table chats drop column average_flood;