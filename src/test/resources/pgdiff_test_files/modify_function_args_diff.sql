
DROP FUNCTION power_number(integer);
CREATE FUNCTION power_number(arg_new integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
begin
    return arg_new * arg_new;
end;
$$;

ALTER FUNCTION power_number(integer) OWNER TO postgres;
