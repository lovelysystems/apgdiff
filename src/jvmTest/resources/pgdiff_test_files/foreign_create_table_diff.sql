
SET search_path = public, pg_catalog;

CREATE FOREIGN TABLE foreign_to_create (
	id bigint
)SERVER myserver
OPTIONS (
    updatable 'false'
);

