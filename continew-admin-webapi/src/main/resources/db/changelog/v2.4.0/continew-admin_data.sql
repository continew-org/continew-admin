-- liquibase formatted sql

-- changeset Charles7c:1
UPDATE `sys_user` SET `password` = '{bcrypt}$2a$10$4jGwK2BMJ7FgVR.mgwGodey8.xR8FLoU1XSXpxJ9nZQt.pufhasSa' WHERE `username` = 'admin';
UPDATE `sys_user` SET `password` = '{bcrypt}$2a$10$meMbyso06lupZjxT88fG8undZo6.DSNUmifRfnnre8r/s13ciq6M6' WHERE `username` = 'test';