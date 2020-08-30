ALTER TYPE bug_status OWNER TO admin;

ALTER TYPE descr_type
	ADD ATTRIBUTE date_create timestamp without time zone;