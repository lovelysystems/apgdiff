
SET search_path = public, pg_catalog;

ALTER TABLE ONLY childtable
	ALTER COLUMN parenttable_id SET DEFAULT 1;
