
SET search_path = public, pg_catalog;

REVOKE ALL ON TABLE view1 FROM public;
GRANT SELECT, INSERT ON TABLE view1 TO public;
