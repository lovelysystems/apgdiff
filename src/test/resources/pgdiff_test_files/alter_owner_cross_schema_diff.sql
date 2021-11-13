
SET search_path = bo, pg_catalog;
ALTER TABLE steps OWNER TO admin;
ALTER VIEW articles OWNER TO postgres;

SET search_path = fe, pg_catalog;
ALTER TABLE steps OWNER TO admin;
ALTER VIEW articles OWNER TO postgres;
