-- Partitioned table with identity column (new schema, changed generation)
CREATE TABLE part_tab (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    data TEXT
) PARTITION BY RANGE (id);

CREATE TABLE part_tab_p1 PARTITION OF part_tab FOR VALUES FROM (1) TO (1000);
CREATE TABLE part_tab_p2 PARTITION OF part_tab FOR VALUES FROM (1000) TO (2000);

