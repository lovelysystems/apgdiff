CREATE OPERATOR public.> (
    FUNCTION = internal.point_gt,
    LEFTARG = point,
    RIGHTARG = point,
    COMMUTATOR = OPERATOR(public.<)
    );

ALTER OPERATOR public.> (point, point) OWNER TO admin;
