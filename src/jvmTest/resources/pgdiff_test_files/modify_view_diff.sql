
SET search_path = public, pg_catalog;

DROP VIEW IF EXISTS testview CASCADE;

CREATE VIEW testview AS
	SELECT testtable.name,
    testtable.id
   FROM public.testtable;
