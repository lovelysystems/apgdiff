-- Partitioned table without exclusion constraint (old schema)
CREATE TABLE part_excl_tab (
    id INT,
    ts TIMESTAMP
) PARTITION BY RANGE (id);

CREATE TABLE part_excl_tab_p1 PARTITION OF part_excl_tab FOR VALUES FROM (1) TO (1000);
CREATE TABLE part_excl_tab_p2 PARTITION OF part_excl_tab FOR VALUES FROM (1000) TO (2000);

