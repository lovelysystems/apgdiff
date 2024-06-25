
SET search_path = public, pg_catalog;

CREATE SEQUENCE alert_alert_id_seq
	AS integer
	START WITH 1
	INCREMENT BY 1
	NO MAXVALUE
	NO MINVALUE
	CACHE 1;
ALTER SEQUENCE alert_alert_id_seq OWNER TO postgres;
