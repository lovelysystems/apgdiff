create schema internal;

create or replace function internal.point_fn (point, point)
    returns boolean
    language sql
    immutable
as $$
    select true
$$;


CREATE OPERATOR public.> (
    FUNCTION = internal.point_fn,
    LEFTARG = point,
    RIGHTARG = point,
    COMMUTATOR = OPERATOR(public.<)
    );

ALTER OPERATOR public.> (point, point) OWNER TO admin;
