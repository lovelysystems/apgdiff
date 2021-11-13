create schema fe;
CREATE VIEW fe.articles AS
SELECT 1;

CREATE TABLE fe.steps (
  id int
);

ALTER VIEW fe.articles OWNER TO admin;
ALTER TABLE fe.steps OWNER TO postgres;


create schema bo;
CREATE VIEW bo.articles AS
SELECT 1;

CREATE TABLE bo.steps (
    id int
);

ALTER VIEW bo.articles OWNER TO admin;
ALTER TABLE bo.steps OWNER TO postgres;
