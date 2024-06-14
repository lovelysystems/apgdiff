CREATE TABLE parent (
    id bigserial NOT NULL
);

CREATE TABLE new_parent (
    id bigserial NOT NULL
);

CREATE TABLE child (
    field1 polygon
)
INHERITS (new_parent);
