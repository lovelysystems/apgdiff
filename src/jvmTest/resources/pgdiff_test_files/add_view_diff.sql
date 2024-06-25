
SET search_path = public, pg_catalog;

CREATE VIEW testview WITH (security_barrier='true') AS
	SELECT testtable.id,
    testtable.name
   FROM public.testtable;
ALTER VIEW testview OWNER TO postgres;
