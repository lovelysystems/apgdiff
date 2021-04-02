CREATE EXTENSION postgres_fdw;
CREATE SERVER myserver FOREIGN DATA WRAPPER postgres_fdw OPTIONS (host 'foo', dbname 'foodb', port '5432');
CREATE FOREIGN TABLE foreign_to_create (
    id bigint
    ) SERVER myserver
    OPTIONS (
    updatable 'false'
    );
