
SET search_path = public, pg_catalog;

ALTER TABLE testtable
	ADD COLUMN col3 boolean NOT NULL,
	ADD COLUMN col4 character(10) NOT NULL,
	ADD COLUMN col5 text NOT NULL,
	ALTER COLUMN col1 SET NOT NULL;
