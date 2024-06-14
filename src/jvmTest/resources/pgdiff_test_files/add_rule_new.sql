create schema x;

create table x.table1
(
    x int
);

CREATE RULE hello_world_added AS
    ON UPDATE TO x.table1 DO
    NOTIFY hello_added;

