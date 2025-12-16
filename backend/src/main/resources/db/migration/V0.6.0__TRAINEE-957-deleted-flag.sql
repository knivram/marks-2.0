drop table deleted_user;
drop table deleted_mark;

alter table "user" add column deleted bool not null default false;
alter table "user" add column deleted_time timestamp default null;

alter table mark add column deleted bool not null default false;
alter table mark add column deleted_time timestamp default null;

alter table subject alter column is_vocational_module set not null;