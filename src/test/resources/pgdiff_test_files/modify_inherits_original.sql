CREATE TABLE parent (
    id bigserial NOT NULL
);

CREATE TABLE child (
    field1 polygon
)
INHERITS (parent);

