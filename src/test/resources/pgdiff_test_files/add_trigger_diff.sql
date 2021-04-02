
CREATE OR REPLACE FUNCTION test_table_trigger() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
begin
	return NEW;
end;
$$;

ALTER FUNCTION test_table_trigger() OWNER TO postgres;

CREATE TRIGGER test_table_trigger
	BEFORE INSERT OR UPDATE ON test_table
	FOR EACH ROW
	EXECUTE PROCEDURE public.test_table_trigger();
