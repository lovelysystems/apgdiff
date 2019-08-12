
CREATE VIEW testview WITH (security_barrier) AS
	SELECT testtable.id, testtable.name FROM testtable;

ALTER VIEW testview OWNER TO fordfrog;

REVOKE ALL ON TABLE testview FROM admin;
GRANT UPDATE ON TABLE testview TO admin;

REVOKE ALL (id) ON TABLE testview FROM admin;
GRANT SELECT (id) ON TABLE testview TO admin;
REVOKE ALL (name) ON TABLE testview FROM admin;
GRANT SELECT (name), INSERT (name) ON TABLE testview TO admin;
