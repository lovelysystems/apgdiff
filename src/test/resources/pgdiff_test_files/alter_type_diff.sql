ALTER TYPE bug_status OWNER TO admin;
COMMENT ON TYPE bug_status IS 'Status of a bug';

ALTER TYPE descr_type
	ADD ATTRIBUTE date_create timestamp without time zone;