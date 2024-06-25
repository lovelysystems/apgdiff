
SET search_path = public, pg_catalog;

ALTER TABLE testtable
	ADD COLUMN field5 boolean DEFAULT false NOT NULL;
