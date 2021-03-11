CREATE OPERATOR public.> (
    FUNCTION = internal.point_gt,
    LEFTARG = point,
    RIGHTARG = point,
    COMMUTATOR = OPERATOR(public.<)
    );

ALTER OPERATOR public.> (point, point) OWNER TO postgres;

CREATE OPERATOR public.>= (
    FUNCTION = internal.point_gte,
    LEFTARG = point,
    RIGHTARG = point,
    COMMUTATOR = OPERATOR(public.<=)
    );

ALTER OPERATOR public.>= (point, point) OWNER TO postgres;