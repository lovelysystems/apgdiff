
ALTER FOREIGN TABLE foreign_to_alter
	ADD COLUMN country_code character varying(5),
	ALTER COLUMN ref2 TYPE character varying(20);
