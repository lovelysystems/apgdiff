
DROP VIEW IF EXISTS foo;

CREATE VIEW foo AS
	SELECT table_name.bar AS "Foo's bar"
   FROM public.table_name;
