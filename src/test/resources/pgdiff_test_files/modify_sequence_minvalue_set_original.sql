CREATE SEQUENCE testseq
    START WITH 1
    INCREMENT BY 10
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;

-- NOTE: the migration of the new min value without a restart will only work if the sequence is above the new min value
-- in real world this would not matter if the diff runs against an existing db since it might be already above minvalue
-- TODO: use setval in generated diff to set the sequence to at least minval
SELECT setval('testseq ', 10000);

