
CREATE SEQUENCE test2_id_seq
	AS integer
	START WITH 1
	INCREMENT BY 1
	NO MAXVALUE
	NO MINVALUE
	CACHE 1;
ALTER SEQUENCE test2_id_seq OWNER TO postgres;

CREATE TABLE test2 (
	id integer DEFAULT nextval('public.test2_id_seq'::regclass) NOT NULL
);

ALTER TABLE test2 OWNER TO postgres;

ALTER SEQUENCE test2_id_seq
	OWNED BY public.test2.id;
