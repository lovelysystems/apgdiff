CREATE FUNCTION power_number(arg_old integer) RETURNS integer
AS
$$
begin
    return arg_old * arg_old;
end;
$$
    LANGUAGE plpgsql;
