CREATE FUNCTION power_number(arg_new integer) RETURNS integer
AS
$$
begin
    return arg_new * arg_new;
end;
$$
    LANGUAGE plpgsql;
