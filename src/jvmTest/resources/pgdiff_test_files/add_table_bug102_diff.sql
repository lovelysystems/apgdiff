
SET search_path = public, pg_catalog;

CREATE TABLE "procedureresult$Operation" (
	id bigint NOT NULL,
	name character varying(255),
	result_id bigint
);

ALTER TABLE "procedureresult$Operation" OWNER TO postgres;

ALTER TABLE "procedureresult$Operation"
	ADD CONSTRAINT "$1" FOREIGN KEY (result_id) REFERENCES public.testtable(field1) ON UPDATE RESTRICT ON DELETE RESTRICT;
