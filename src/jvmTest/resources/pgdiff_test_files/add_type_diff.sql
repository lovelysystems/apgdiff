
CREATE TYPE bug_status AS ENUM (
	'new',
	'open',
	'closed'
);

ALTER TYPE bug_status OWNER TO postgres;

CREATE TYPE descr_type AS (
	name text,
	amount integer
);

ALTER TYPE descr_type OWNER TO postgres;

CREATE TABLE t1 (
	id integer,
	descr public.descr_type
);

ALTER TABLE t1 OWNER TO postgres;
