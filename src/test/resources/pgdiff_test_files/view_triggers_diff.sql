DROP TRIGGER IF EXISTS trg_testview_instead_of_delete ON testview;

DROP TRIGGER IF EXISTS trg_testview_instead_of_insert ON testview;

CREATE TRIGGER trg_testview_instead_of_delete_new_name
	INSTEAD OF DELETE ON testview
	FOR EACH ROW
	EXECUTE PROCEDURE fn_trg_testview();

