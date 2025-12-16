--                                     Spring 22                               Autumn 22                               Spring 23
delete from semester where id not in ('00d94e70-18fc-49dd-bb38-99c79d8afc24', 'e9b6a70e-87cd-464b-97a6-bd15c611bcdd', '66f53a81-0826-49b6-9d1a-0591617dbd63', '50010262-4dc8-427e-b9f1-99a002fe82af');
--                                    Englisch                                French                                  Math
delete from subject where id not in ('c0b7270d-49c6-4ab2-af0f-af3371745fed', '09f526bc-24ba-4ed9-988b-bac98d670112', '37bad6f5-461a-4d50-9074-02c95879148f');

-- user with no supervisor
insert into "user" (id, username, email) VALUES ('a84d99a8-ab76-4924-a137-22a3705c77f8', 'unusedUser', 'unused.user@ergon.ch');

-- marks for user without supervisor
insert into mark (id, user_id, subject_id, semester_id, name, date, weight, value)
values ('11e032f5-5b03-4df3-b78f-b6efda42275c', 'a84d99a8-ab76-4924-a137-22a3705c77f8', '09f526bc-24ba-4ed9-988b-bac98d670112', '00d94e70-18fc-49dd-bb38-99c79d8afc24', 'Voci Test', '2022-04-20', 1000, 5000),
       ('2d7fa523-43d2-4c3e-a4c6-0fd0317d7c14', 'a84d99a8-ab76-4924-a137-22a3705c77f8', '37bad6f5-461a-4d50-9074-02c95879148f', 'e9b6a70e-87cd-464b-97a6-bd15c611bcdd', 'Sin, Cos, Tan', '2022-10-23', 1500, 5500),
       ('a838e911-ec43-48c6-8fd9-be079d6ff333', 'a84d99a8-ab76-4924-a137-22a3705c77f8', 'c0b7270d-49c6-4ab2-af0f-af3371745fed', '66f53a81-0826-49b6-9d1a-0591617dbd63', 'Past Simple',   '2023-05-01', 1000, 4750);