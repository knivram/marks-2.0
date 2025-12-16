create type "public"."user_role" as enum ('APPRENTICE', 'SUPERVISOR', 'ADMIN');

alter table "user" add column role "public"."user_role";

update "user" set role = 'ADMIN' where isadmin;
update "user" set role = 'SUPERVISOR' where exists(select 1 from apprentice_supervisor where fk_supervisor = "user".id);
update "user" set role = 'APPRENTICE' where "user".role is null;

alter table "user" alter column role set not null;
alter table "user" drop column isadmin;