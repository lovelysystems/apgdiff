
SET search_path = public, pg_catalog;

ALTER TABLE items ENABLE ROW LEVEL SECURITY;

ALTER TABLE projects FORCE ROW LEVEL SECURITY;
