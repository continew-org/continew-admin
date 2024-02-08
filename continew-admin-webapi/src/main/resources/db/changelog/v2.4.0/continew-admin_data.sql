-- liquibase formatted sql

-- changeset Charles7c:1
UPDATE `sys_user` SET `password` = '{bcrypt}$2a$10$4jGwK2BMJ7FgVR.mgwGodey8.xR8FLoU1XSXpxJ9nZQt.pufhasSa' WHERE `username` = 'admin';
UPDATE `sys_user` SET `password` = '{bcrypt}$2a$10$meMbyso06lupZjxT88fG8undZo6.DSNUmifRfnnre8r/s13ciq6M6' WHERE `username` = 'test';

-- changeset Charles7c:2
UPDATE `sys_user` SET `email` = '42190c6c5639d2ca4edb4150a35e058559ccf8270361a23745a2fd285a273c28' WHERE `username` = 'admin';
UPDATE `sys_user` SET `phone` = '5bda89a4609a65546422ea56bfe5eab4' WHERE `username` = 'admin';
