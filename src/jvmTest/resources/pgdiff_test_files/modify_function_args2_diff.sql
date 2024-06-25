
SET search_path = public, pg_catalog;

DROP FUNCTION multiply_numbers(integer, integer);
CREATE FUNCTION multiply_numbers(number2 integer, number1 integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
begin
    return number2 * number1;
end;
$$;

ALTER FUNCTION multiply_numbers(integer, integer) OWNER TO postgres;
