CREATE TABLE deleted_user
(
    id              UUID PRIMARY KEY,
    username        VARCHAR(64)  NOT NULL,
    email           VARCHAR(128) NOT NULL unique,
    isAdmin         BOOL         NOT NULL DEFAULT false,
    deleted_time    TIMESTAMP    NOT NULL DEFAULT now()::timestamp
);

ALTER TABLE apprentice_supervisor drop constraint apprentice_supervisor_fk_apprentice_fkey;
ALTER TABLE apprentice_supervisor add foreign key (fk_apprentice) references "user"(id) on delete cascade;
ALTER TABLE apprentice_supervisor drop constraint apprentice_supervisor_fk_supervisor_fkey;
ALTER TABLE apprentice_supervisor add foreign key (fk_supervisor) references "user"(id) on delete cascade;

ALTER TABLE deleted_mark drop constraint deleted_mark_user_id_fkey;