
CREATE SEQUENCE new_parent_id_seq
	START WITH 1
	INCREMENT BY 1
	NO MAXVALUE
	NO MINVALUE
	CACHE 1;
ALTER SEQUENCE new_parent_id_seq OWNER TO postgres;

CREATE TABLE new_parent (
	id bigint DEFAULT nextval('public.new_parent_id_seq'::regclass) NOT NULL
);

ALTER TABLE new_parent OWNER TO postgres;

ALTER TABLE child
	INHERIT new_parent;

ALTER TABLE child
	NO INHERIT parent;

ALTER TABLE ONLY child
	ALTER COLUMN id SET DEFAULT nextval('public.new_parent_id_seq'::regclass);

ALTER SEQUENCE new_parent_id_seq
	OWNED BY public.new_parent.id;
