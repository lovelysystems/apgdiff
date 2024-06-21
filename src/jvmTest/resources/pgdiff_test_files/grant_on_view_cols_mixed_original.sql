create table items(id integer, name text);

create view items_view as select id, name from items;
