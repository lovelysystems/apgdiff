
CREATE OR REPLACE FUNCTION return_one() RETURNS integer
    LANGUAGE plpgsql
    AS $$
begin
	return 1;
end;
$$;

ALTER FUNCTION return_one() OWNER TO postgres;
