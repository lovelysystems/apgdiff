CREATE OPERATOR >= (
    FUNCTION = internal.point_gte,
    LEFTARG = point,
    RIGHTARG = point,
    COMMUTATOR = OPERATOR(public.<=)
    );

ALTER OPERATOR >= (point, point) OWNER TO postgres;
