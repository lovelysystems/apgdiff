create table table_name
(
    foo  text,
    bar  text,
    bar2 text
);

CREATE VIEW foo AS
SELECT bar AS "Foo's bar"
FROM table_name;