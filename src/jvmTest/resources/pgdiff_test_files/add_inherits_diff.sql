
SET search_path = public, pg_catalog;

CREATE TABLE testtable (
	field1 polygon
)
INHERITS (parenttable);

ALTER TABLE ONLY testtable
	ALTER COLUMN id SET DEFAULT nextval('public.parenttable_id_seq'::regclass);

ALTER TABLE testtable OWNER TO postgres;
