
SET search_path = public, pg_catalog;

ALTER TABLE table1
	ADD COLUMN col2 integer NOT NULL,
	ADD COLUMN col3 integer DEFAULT 5 NOT NULL;
