CREATE TABLE testtable (
    id bigint,
    name character varying(30)
);


CREATE VIEW testview AS
    SELECT testtable.id, testtable.name FROM testtable;

CREATE FUNCTION fn_trg_testview() RETURNS trigger LANGUAGE plpgsql
AS $$
BEGIN
	-- do nothing
	RETURN OLD;
END;
$$;

CREATE TRIGGER trg_testview_instead_of_delete INSTEAD OF DELETE ON testview FOR EACH ROW EXECUTE PROCEDURE fn_trg_testview();
CREATE TRIGGER trg_testview_instead_of_insert INSTEAD OF INSERT ON testview FOR EACH ROW EXECUTE PROCEDURE fn_trg_testview();
CREATE TRIGGER trg_testview_instead_of_update INSTEAD OF UPDATE ON testview FOR EACH ROW EXECUTE PROCEDURE fn_trg_testview();

