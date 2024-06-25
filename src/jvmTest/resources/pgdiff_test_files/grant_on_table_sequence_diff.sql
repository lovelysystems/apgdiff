
SET search_path = public, pg_catalog;

REVOKE ALL ON SEQUENCE table1_id_seq FROM public;
GRANT SELECT, USAGE ON SEQUENCE table1_id_seq TO public;

REVOKE ALL ON TABLE table1 FROM public;
GRANT SELECT, UPDATE ON TABLE table1 TO public;
