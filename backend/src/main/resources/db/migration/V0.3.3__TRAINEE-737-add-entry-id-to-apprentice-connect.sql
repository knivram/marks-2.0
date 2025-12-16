alter table apprentice_supervisor add COLUMN id uuid;

update apprentice_supervisor
set id = 'e0466a09-cf7c-4fd2-87e3-6813544f8da8'
where apprentice_supervisor.fk_supervisor = 'fe35edb5-c1c2-443e-8a52-90ce8cd83039';

update apprentice_supervisor
set id = '7fbb6de7-a80c-422c-bd07-4cb5f9de8476'
where fk_supervisor = 'e6b170aa-473c-4e3a-8442-fcaf094ec008';

update apprentice_supervisor
set id = '9df6e01d-8ebf-417b-905d-06df3d3ace4d'
where fk_supervisor = '56e97688-df0e-427c-8029-35ae8d5fb9c6';

update apprentice_supervisor
set id = '22cdfded-3a6c-41e7-a7be-4d4371681089'
where fk_supervisor = 'b0a715a4-78ac-4b5c-83be-29ed2f2f0839';

update apprentice_supervisor
set id = '0abc0228-374e-41f2-9ff6-6a88c2c1089c'
where fk_supervisor = '2c2d523e-a2ef-4f72-ae97-b71ee7ae17a3';

update apprentice_supervisor
set id = '268a0ede-00a1-4d19-a572-2b76b7bc0c48'
where fk_supervisor = '7d4db3ce-880c-4cba-bae1-7ec952d5d1ec';

alter table apprentice_supervisor add primary key (id);