
SET search_path = public, pg_catalog;

DROP VIEW IF EXISTS foo CASCADE;

CREATE VIEW foo AS
	SELECT table_name.bar AS "Foo's bar"
   FROM public.table_name;
