
DROP MATERIALIZED VIEW testview;

CREATE MATERIALIZED VIEW testview AS
	SELECT testtable.name,
    testtable.id
   FROM public.testtable
  WITH NO DATA;
