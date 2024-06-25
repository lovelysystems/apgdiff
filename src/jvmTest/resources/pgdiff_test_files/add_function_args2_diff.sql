
SET search_path = public, pg_catalog;

CREATE OR REPLACE FUNCTION multiply_numbers(number1 integer, number2 integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
begin
	return number1 * number2;
end;
$$;

ALTER FUNCTION multiply_numbers(integer, integer) OWNER TO postgres;
