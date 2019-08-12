create table items(id integer, name text);

create view items_view as select id, name from items;

grant select(id, name), insert(name), update on items_view to admin;
