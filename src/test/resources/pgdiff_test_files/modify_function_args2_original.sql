CREATE FUNCTION multiply_numbers(number1 integer, number2 integer) RETURNS integer
AS
$$
begin
    return number1 * number2;
end;
$$
    LANGUAGE plpgsql;

ALTER FUNCTION public.multiply_numbers(integer, integer) OWNER TO postgres;
