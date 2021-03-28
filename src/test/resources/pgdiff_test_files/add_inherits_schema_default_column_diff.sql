
CREATE SCHEMA schema1;

ALTER SCHEMA schema1 OWNER TO postgres;

SET search_path = schema1, pg_catalog;

CREATE TABLE childtable (
	childtable_date timestamp with time zone NOT NULL
)
INHERITS (public.parenttable);

ALTER TABLE ONLY childtable
	ALTER COLUMN parenttable_id SET DEFAULT 0;

ALTER TABLE childtable OWNER TO postgres;
