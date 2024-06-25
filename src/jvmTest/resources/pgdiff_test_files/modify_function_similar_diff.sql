
SET search_path = public, pg_catalog;

DROP FUNCTION multiply_numbers(smallint, smallint);
CREATE FUNCTION multiply_numbers(number1 smallint, number2 smallint) RETURNS smallint
    LANGUAGE plpgsql
    AS $$
begin
        return number1 * number2;
end;
$$;

ALTER FUNCTION multiply_numbers(smallint, smallint) OWNER TO postgres;
