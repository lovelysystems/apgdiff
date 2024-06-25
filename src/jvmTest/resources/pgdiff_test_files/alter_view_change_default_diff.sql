
SET search_path = public, pg_catalog;

ALTER TABLE test ALTER COLUMN test_col SET DEFAULT '2020-01-01 00:00:00+00'::timestamp with time zone;
