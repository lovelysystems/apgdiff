
CREATE SEQUENCE table2_col1_seq
	START WITH 1
	INCREMENT BY 1
	NO MAXVALUE
	NO MINVALUE
	CACHE 1;
ALTER SEQUENCE table2_col1_seq OWNER TO postgres;

CREATE TABLE table2 (
	col1 integer DEFAULT nextval('public.table2_col1_seq'::regclass) NOT NULL
);

ALTER TABLE table2 OWNER TO postgres;

ALTER SEQUENCE table2_col1_seq
	OWNED BY public.table2.col1;
