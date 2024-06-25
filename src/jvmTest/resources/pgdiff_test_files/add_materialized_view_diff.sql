
SET search_path = public, pg_catalog;

CREATE MATERIALIZED VIEW testview AS
	SELECT testtable.id,
    testtable.name
   FROM public.testtable
  WITH NO DATA;
ALTER MATERIALIZED VIEW testview OWNER TO postgres;
