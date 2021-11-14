
SET search_path = x, pg_catalog;

CREATE RULE hello_world_added AS
 ON UPDATE TO x.table1
 DO
 NOTIFY hello_added;
DROP RULE IF EXISTS notify_me ON x.table1;
