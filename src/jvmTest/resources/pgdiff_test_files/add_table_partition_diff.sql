
SET search_path = public, pg_catalog;

CREATE TABLE testtable2 (
	id integer,
	name character varying(100) NOT NULL
)
PARTITION BY RANGE (name);

ALTER TABLE testtable2 OWNER TO postgres;
