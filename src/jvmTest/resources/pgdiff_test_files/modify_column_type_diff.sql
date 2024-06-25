
SET search_path = public, pg_catalog;

ALTER TABLE testtable
	ALTER COLUMN field1 TYPE integer USING field1::integer,
	ALTER COLUMN field3 TYPE character varying(150) USING field3::character varying(150);
