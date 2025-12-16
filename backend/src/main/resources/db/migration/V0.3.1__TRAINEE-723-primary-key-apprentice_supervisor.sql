alter table apprentice_supervisor add unique (fk_apprentice, fk_supervisor);
alter table "user" add unique (username)