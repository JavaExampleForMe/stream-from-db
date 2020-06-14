--liquibase formatted sql
--changeset idaa:1 endDelimiter:GO
--comment :cscscscscc dcsc ccd
--validCheckSum: 8:b468475eb75f3573ad18179e0910024c
create table cars (
    id int primary key,
    employeeId int ,
    type int ,
    text1 varchar(255)
)
GO
--changeset idaa:2 endDelimiter:GO
insert into cars (id, employeeId, type, text1) values (1, 3 , 6, 'name 1')
GO
insert into cars (id, employeeId, type, text1) values (2, 3 , 6, 'name 2')
GO
