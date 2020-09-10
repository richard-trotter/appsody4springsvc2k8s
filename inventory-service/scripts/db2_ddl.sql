create database inventry;

drop table items;

create table items (
  id int not null GENERATED ALWAYS AS IDENTITY (START WITH 13401) primary key,
  stock int not null,
  name varchar(100) not null,
  description varchar(1800) not null,
  price decimal(8,2) not null,
  img_alt varchar(75),
  img varchar(50) not null
);
