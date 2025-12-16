-- Add some normal subjects
INSERT INTO subject (id, name)
VALUES ('09f526bc-24ba-4ed9-988b-bac98d670112', 'Französisch'),
       ('37bad6f5-461a-4d50-9074-02c95879148f', 'Mathematik'),
       ('c0b7270d-49c6-4ab2-af0f-af3371745fed', 'Englisch'),
       ('8cffc1cb-0dfe-472e-8720-a8cc3b80dd75', 'Chemie'),
       ('1608d0ba-a120-465c-aa16-c101e7a0c260', 'Physik'),
       ('01842f26-5662-45f6-beb8-58dbd1731fb9', 'Biologie'),
       ('15033e6d-d460-4eea-9185-f5f2e710dba4', 'Deutsch'),
       ('b963a0bd-567e-4f6f-a9e2-39d76464619b', 'Sport'),
       ('db6d501f-836e-40e0-8c4a-6e16a7f83912', 'Geschichte'),
       ('ffd46767-a3f4-46c7-bbff-d49003ff8520', 'Geografie'),
       ('ef81a327-d843-45c0-8d38-46fe67b7cf42', 'Zeichnen'),
       ('3530465e-c53b-4f12-8471-e5cc18317447', 'Musik');

-- Add a fue semesters
INSERT INTO semester(id, semester_name, semester_start, semester_end)
VALUES ('c6ddfc93-f02e-46b8-a217-ed7eb47fbca1', 'Herbstsemester',     '2019-08-18', '2020-02-01'),
       ('8be7d377-94cc-4d02-9cfc-281495a6e810', 'Frühlingssemester',  '2020-02-16', '2020-07-11'),
       ('1fd6f620-cc9e-4adb-88e8-877ca0931037', 'Herbstsemester',     '2020-08-16', '2021-02-06'),
       ('1f0e159e-e5cb-451c-9475-a2568b78deac', 'Frühlingssemester',  '2021-02-21', '2021-07-17'),
       ('da119d99-4a96-4ed7-ab7f-2ebd682495b2', 'Herbstsemester',     '2021-08-22', '2022-02-05'),
       ('00d94e70-18fc-49dd-bb38-99c79d8afc24', 'Frühlingssemester',  '2022-02-20', '2022-07-16'),
       ('e9b6a70e-87cd-464b-97a6-bd15c611bcdd', 'Herbstsemester',     '2022-08-21', '2023-02-06'),
       ('66f53a81-0826-49b6-9d1a-0591617dbd63', 'Frühlingssemester',  '2023-02-19', '2023-07-17'),
       ('50010262-4dc8-427e-b9f1-99a002fe82af', 'Herbstsemester',     '2023-08-20', '2024-02-05'),
       ('3123f16f-d25e-4473-a582-a6a06cf88fbc', 'Frühlingssemester',  '2024-02-18', '2024-07-15');

-- Add Admin
INSERT INTO "user" (id, username, email, isadmin) 
VALUES ('28373b11-ad87-4fe3-994f-970c053b95d8', 'berlie', 'bernd.lienberger@ergon.ch', true);

-- Add Users
INSERT INTO "user" (id, username, email)
VALUES ('80b5f44f-6e83-43e0-9211-9fcdfbdc5ac1', 'mkramer', 'marvin.kramer@ergon.ch'),
       ('fe35edb5-c1c2-443e-8a52-90ce8cd83039', 'boxi', 'thomas.boksberger@ergon.ch'),
       ('eae33934-8c17-4f64-a36a-ce50dab9c191', 'jsigrist', 'jan.sigrist@ergon.ch'),
       ('e6b170aa-473c-4e3a-8442-fcaf094ec008', 'pgruntz', 'patrick.gruntz@ergon.ch'),
       ('56e97688-df0e-427c-8029-35ae8d5fb9c6', 'acatala', 'alvaro.catala@ergon.ch'),
       ('2bcf1294-d294-4266-b1ee-d72f6be7e847', 'kevglatt', 'kevin.glatthard@ergon.ch'),
       ('b0a715a4-78ac-4b5c-83be-29ed2f2f0839', 'noah', 'noah.heusser@ergon.ch'),
       ('7109e9f5-972c-4043-97b5-280e4ea1fbf3', 'fliberto', 'fabio.liberto@ergon.ch'),
       ('2c2d523e-a2ef-4f72-ae97-b71ee7ae17a3', 'tkrebs', 'tobias.krebs@ergon.ch'),
       ('7d4db3ce-880c-4cba-bae1-7ec952d5d1ec', 'dam', 'daniela.meier@ergon.ch');

-- Connect apprentice to supervisor
INSERT INTO apprentice_supervisor (fk_apprentice, fk_supervisor)
VALUES -- mkramer -> boxi
       ('80b5f44f-6e83-43e0-9211-9fcdfbdc5ac1', 'fe35edb5-c1c2-443e-8a52-90ce8cd83039'),
       --jsigirst -> pgruntz
       ('eae33934-8c17-4f64-a36a-ce50dab9c191', 'e6b170aa-473c-4e3a-8442-fcaf094ec008'),
       --jsigirst -> acatala
       ('eae33934-8c17-4f64-a36a-ce50dab9c191', '56e97688-df0e-427c-8029-35ae8d5fb9c6'),
       --kevglatt -> noah
       ('2bcf1294-d294-4266-b1ee-d72f6be7e847', 'b0a715a4-78ac-4b5c-83be-29ed2f2f0839'),
       --fliberto -> tkrebs
       ('7109e9f5-972c-4043-97b5-280e4ea1fbf3', '2c2d523e-a2ef-4f72-ae97-b71ee7ae17a3'),
       --fliberto -> dam
       ('7109e9f5-972c-4043-97b5-280e4ea1fbf3', '7d4db3ce-880c-4cba-bae1-7ec952d5d1ec');