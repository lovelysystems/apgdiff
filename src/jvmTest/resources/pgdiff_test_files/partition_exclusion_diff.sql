ALTER TABLE part_excl_tab ADD EXCLUDE USING gist (ts WITH =);

