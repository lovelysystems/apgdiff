
SET search_path = public, pg_catalog;

CREATE SEQUENCE test_id_seq
	START WITH 1
	INCREMENT BY 1
	MAXVALUE 2147483647
	NO MINVALUE
	CACHE 1;
ALTER SEQUENCE test_id_seq OWNER TO postgres;
