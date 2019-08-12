create table items(id integer, name text);

create view items_view as select id from items;

grant select(id), update on items_view to admin;
