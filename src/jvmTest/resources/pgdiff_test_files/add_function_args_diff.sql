
SET search_path = public, pg_catalog;

CREATE OR REPLACE FUNCTION power_number("input" integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
begin
	return input * input;
end;
$$;

ALTER FUNCTION power_number(integer) OWNER TO postgres;
