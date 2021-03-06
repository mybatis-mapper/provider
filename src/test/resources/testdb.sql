drop table user if exists;
drop table role if exists;

create table user
(
    id   INTEGER GENERATED BY DEFAULT AS IDENTITY (START WITH 1) PRIMARY KEY,
    name VARCHAR(32) DEFAULT 'DEFAULT',
    sex  VARCHAR(2)
);

create table role
(
    id   INTEGER GENERATED BY DEFAULT AS IDENTITY (START WITH 1) PRIMARY KEY,
    name VARCHAR(32) NOT NULL
);

insert into user(id, name, sex)
values (1, '张无忌', '男'),
       (2, '赵敏', '女'),
       (3, '周芷若', '女'),
       (4, '小昭', '女'),
       (5, '殷离', '女');

insert into role(id, name)
values (1, '男主角'),
       (2, '女主角'),
       (3, '配角');