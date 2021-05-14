
ALTER TABLE agent
	ADD COLUMN abc bigint;
COMMENT ON COLUMN agent.id IS 'This ID support schema name';
COMMENT ON COLUMN agent.abc IS 'This agent supports credit system or not.';
