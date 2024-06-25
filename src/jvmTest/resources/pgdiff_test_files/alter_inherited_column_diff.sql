
SET search_path = public, pg_catalog;

ALTER TABLE ONLY childtable
	ALTER COLUMN a SET DEFAULT 'child a'::text;

ALTER TABLE ONLY grandchildtable
	ALTER COLUMN a SET DEFAULT 'grandchild a'::text;
