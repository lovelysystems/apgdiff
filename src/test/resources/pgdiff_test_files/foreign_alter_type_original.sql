CREATE EXTENSION postgres_fdw;
CREATE SERVER myserver FOREIGN DATA WRAPPER postgres_fdw OPTIONS (host 'foo', dbname 'foodb', port '5432');

CREATE FOREIGN TABLE foreign_to_alter (
    id bigint,
    user_id bigint,
    ref1 character varying(60),
    ref2 character varying(60),
    inplay_stake_coef numeric,
    pre_match_stake_coef numeric,
    punter_limits character varying,
    name character varying(60),
    colour_cat character varying(30)
)
SERVER myserver
OPTIONS (
    updatable 'false'
);