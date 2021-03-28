create table table_name
(
    foo  text,
    bar  text,
    bar2 text
);

CREATE VIEW foo AS
SELECT bar AS "Foo's bar", bar2 AS "Foo's second bar"
FROM table_name;