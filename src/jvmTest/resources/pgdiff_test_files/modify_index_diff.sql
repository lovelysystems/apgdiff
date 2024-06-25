
SET search_path = public, pg_catalog;

DROP INDEX testindex;

CREATE INDEX testindex ON testtable USING btree (field3);
