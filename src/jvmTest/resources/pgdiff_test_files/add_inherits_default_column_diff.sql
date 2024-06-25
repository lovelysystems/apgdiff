
SET search_path = public, pg_catalog;

CREATE TABLE childtable (
)
INHERITS (parenttable);

ALTER TABLE ONLY childtable
	ALTER COLUMN parenttable_id SET DEFAULT 0;

ALTER TABLE childtable OWNER TO postgres;
