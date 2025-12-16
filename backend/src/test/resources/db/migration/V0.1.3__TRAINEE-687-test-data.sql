-- Marks
INSERT INTO mark (id, user_id, subject_id, name, date, weight, value, semester_id)
values  ('087db115-3b23-46ca-b44d-8ef4eca28965', '80b5f44f-6e83-43e0-9211-9fcdfbdc5ac1', '09f526bc-24ba-4ed9-988b-bac98d670112', 'Voci-Test',     '2022-04-20', 1000, 5000, '00d94e70-18fc-49dd-bb38-99c79d8afc24'),
        ('fb8fefeb-a29d-4589-8329-1578c53567e0', '80b5f44f-6e83-43e0-9211-9fcdfbdc5ac1', '09f526bc-24ba-4ed9-988b-bac98d670112', 'Voci-Test 2',     '2022-12-11', 1000, 5000, 'e9b6a70e-87cd-464b-97a6-bd15c611bcdd'),
        ('bf8ef84a-c5c6-4933-ad85-50809fe8eb49', '80b5f44f-6e83-43e0-9211-9fcdfbdc5ac1', '37bad6f5-461a-4d50-9074-02c95879148f', 'Sin, Cos, Tan', '2022-10-23', 1500, 5500, 'e9b6a70e-87cd-464b-97a6-bd15c611bcdd'),
        ('31c9909c-f0dd-425a-892d-c44791342874', '80b5f44f-6e83-43e0-9211-9fcdfbdc5ac1', '37bad6f5-461a-4d50-9074-02c95879148f', 'Addition und Subtraktion', '2022-12-23', 1500, 5500, 'e9b6a70e-87cd-464b-97a6-bd15c611bcdd'),
        ('35e683bc-4051-4b97-80ab-7f50588c0611', '80b5f44f-6e83-43e0-9211-9fcdfbdc5ac1', 'c0b7270d-49c6-4ab2-af0f-af3371745fed', 'Past Simple',   '2023-05-01', 1000, 4750, '66f53a81-0826-49b6-9d1a-0591617dbd63');

-- add future & past marks without marks
insert into mark (id, user_id, subject_id, name, date, weight, semester_id)
values ('14223a88-a262-4347-9dc8-d176c7fd9c7e', '80b5f44f-6e83-43e0-9211-9fcdfbdc5ac1','09f526bc-24ba-4ed9-988b-bac98d670112', 'Französisch Verben', '2024-01-24', 1250, '50010262-4dc8-427e-b9f1-99a002fe82af'),
       ('684df94c-d874-45bb-ae7f-da3e019ec58c', '80b5f44f-6e83-43e0-9211-9fcdfbdc5ac1','37bad6f5-461a-4d50-9074-02c95879148f', 'Kurzen von Brüchen', '2023-12-02', 1000, '50010262-4dc8-427e-b9f1-99a002fe82af'),
       ('1861801a-7861-4851-96a3-81d71a7a65fe', '80b5f44f-6e83-43e0-9211-9fcdfbdc5ac1','c0b7270d-49c6-4ab2-af0f-af3371745fed', 'Vocitest', '2023-04-04', 500, '66f53a81-0826-49b6-9d1a-0591617dbd63'),
       ('f41fa713-afb0-48bc-9357-3182726e50be', '80b5f44f-6e83-43e0-9211-9fcdfbdc5ac1','c0b7270d-49c6-4ab2-af0f-af3371745fed', 'Future', '2023-04-04', 750, '66f53a81-0826-49b6-9d1a-0591617dbd63');