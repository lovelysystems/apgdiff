CREATE TYPE bug_status AS ENUM (
	'new',
	'open',
	'closed'
);

ALTER TYPE bug_status OWNER TO dv;

CREATE TYPE descr_type AS (
	name text,
	amount integer
);

ALTER TYPE descr_type OWNER TO dv;

CREATE TABLE IF NOT EXISTS t1 (
	id integer,
	descr descr_type
);

ALTER TABLE t1 OWNER TO dv;
