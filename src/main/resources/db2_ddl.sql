create database inventry;

drop table items;

select count(*) from SYSCAT.TABLES where TABNAME='ITEMS';

create table items (
  id int not null GENERATED ALWAYS AS IDENTITY (START WITH 13401) primary key,
  stock int not null,
  name varchar(100) not null,
  description varchar(2048) not null,
  price decimal(8,2) not null,
  img_alt varchar(75),
  img varchar(50) not null
);

