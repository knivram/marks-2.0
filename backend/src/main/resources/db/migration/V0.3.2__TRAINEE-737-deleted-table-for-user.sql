CREATE TABLE deleted_user
(
    id              UUID PRIMARY KEY,
    username        VARCHAR(64)  NOT NULL,
    email           VARCHAR(128) NOT NULL unique,
    isAdmin         BOOL         NOT NULL DEFAULT false,
    deleted_time    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Drop and recreate foreign keys with ON DELETE CASCADE.
-- Use column-based drop syntax for portability across PostgreSQL and H2.
ALTER TABLE apprentice_supervisor DROP CONSTRAINT IF EXISTS apprentice_supervisor_fk_apprentice_fkey;
ALTER TABLE apprentice_supervisor ADD FOREIGN KEY (fk_apprentice) REFERENCES "user"(id) ON DELETE CASCADE;
ALTER TABLE apprentice_supervisor DROP CONSTRAINT IF EXISTS apprentice_supervisor_fk_supervisor_fkey;
ALTER TABLE apprentice_supervisor ADD FOREIGN KEY (fk_supervisor) REFERENCES "user"(id) ON DELETE CASCADE;

ALTER TABLE deleted_mark DROP CONSTRAINT IF EXISTS deleted_mark_user_id_fkey;
