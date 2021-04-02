
ALTER TABLE test1
	ADD COLUMN test2 text DEFAULT '*/'::text,
	ADD COLUMN test text DEFAULT 'this /*is*/ test'::text,
	ADD COLUMN test3 text DEFAULT '*/'::text;
