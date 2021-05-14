
CREATE SCHEMA testschema2;

ALTER SCHEMA testschema2 OWNER TO postgres;

SET search_path = public, pg_catalog;

DROP TABLE IF EXISTS testtable2;

DROP SEQUENCE IF EXISTS testtable2_id_seq;

CREATE SEQUENCE testtable3_id_seq
	START WITH 1
	INCREMENT BY 1
	NO MAXVALUE
	NO MINVALUE
	CACHE 1;
ALTER SEQUENCE testtable3_id_seq OWNER TO postgres;

CREATE TABLE testtable3 (
	id bigint DEFAULT nextval('public.testtable3_id_seq'::regclass) NOT NULL
);

ALTER TABLE testtable3 OWNER TO postgres;

ALTER SEQUENCE testtable3_id_seq
	OWNED BY public.testtable3.id;

SET search_path = testschema2, pg_catalog;

CREATE SEQUENCE testtable1_id_seq
	START WITH 1
	INCREMENT BY 1
	NO MAXVALUE
	NO MINVALUE
	CACHE 1;
ALTER SEQUENCE testtable1_id_seq OWNER TO postgres;

CREATE TABLE testtable1 (
	id integer DEFAULT nextval('testschema2.testtable1_id_seq'::regclass) NOT NULL
);

ALTER TABLE testtable1 OWNER TO postgres;

ALTER SEQUENCE testtable1_id_seq
	OWNED BY testschema2.testtable1.id;

DROP SCHEMA testschema1 CASCADE;
