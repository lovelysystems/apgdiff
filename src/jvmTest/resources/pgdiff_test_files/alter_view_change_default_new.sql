CREATE VIEW test AS
SELECT current_timestamp as test_col;

ALTER VIEW test ALTER COLUMN test_col SET DEFAULT '2020-01-01';
