
SET search_path = public, pg_catalog;

REVOKE ALL ON TABLE items FROM admin;
GRANT SELECT, UPDATE ON TABLE items TO admin;

REVOKE ALL ON TABLE items FROM webuser;
GRANT INSERT, DELETE ON TABLE items TO webuser;

REVOKE ALL (id) ON TABLE items FROM admin;
GRANT SELECT (id) ON TABLE items TO admin;
REVOKE ALL (id) ON TABLE items FROM webuser;
GRANT SELECT (id) ON TABLE items TO webuser;
REVOKE ALL (name) ON TABLE items FROM admin;
GRANT SELECT (name), INSERT (name), UPDATE (name) ON TABLE items TO admin;
REVOKE ALL (name) ON TABLE items FROM webuser;
GRANT SELECT (name), UPDATE (name) ON TABLE items TO webuser;
