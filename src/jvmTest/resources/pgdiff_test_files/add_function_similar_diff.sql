
SET search_path = public, pg_catalog;

CREATE OR REPLACE FUNCTION multiply_numbers(number2 smallint, number1 smallint) RETURNS smallint
    LANGUAGE plpgsql
    AS $$
begin
        return number2 * number1;
end;
$$;

ALTER FUNCTION multiply_numbers(smallint, smallint) OWNER TO postgres;
