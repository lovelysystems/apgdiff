
SET search_path = public, pg_catalog;

CREATE SEQUENCE task_id_seq
	START WITH 1
	INCREMENT BY 1
	NO MAXVALUE
	NO MINVALUE
	CACHE 1;
REVOKE ALL ON SEQUENCE task_id_seq FROM webuser;
GRANT USAGE ON SEQUENCE task_id_seq TO webuser;
ALTER SEQUENCE task_id_seq OWNER TO postgres;
