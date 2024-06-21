
SET search_path = public, pg_catalog;

CREATE OPERATOR >= (
    FUNCTION = internal.point_fn,
    LEFTARG = point,
    RIGHTARG = point,
    COMMUTATOR = OPERATOR(public.<=)
    );

ALTER OPERATOR >= (point, point) OWNER TO postgres;

SET search_path = internal, pg_catalog;

CREATE OR REPLACE FUNCTION point_fn(point, point) RETURNS boolean
    LANGUAGE sql IMMUTABLE
    AS $$
select true
$$;

ALTER FUNCTION point_fn(point, point) OWNER TO postgres;
