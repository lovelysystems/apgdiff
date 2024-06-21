create schema x;

create table x.table1
(
    x int
);

CREATE RULE notify_me AS
    ON UPDATE TO x.table1 DO
    NOTIFY hello;
