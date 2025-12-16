alter table subject add column is_vocational_module BOOL;

update subject SET is_vocational_module = false;

update subject SET is_vocational_module = true where name like 'M___'