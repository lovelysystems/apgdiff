
DROP TRIGGER trg_testview_instead_of_delete ON testview;

DROP TRIGGER trg_testview_instead_of_insert ON testview;

CREATE TRIGGER trg_testview_instead_of_delete_new_name
	INSTEAD OF DELETE ON testview
	FOR EACH ROW
	EXECUTE PROCEDURE public.fn_trg_testview();
