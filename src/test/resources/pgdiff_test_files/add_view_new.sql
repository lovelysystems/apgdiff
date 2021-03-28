--
-- PostgreSQL database dump
--

SET client_encoding = 'UTF8';
SET standard_conforming_strings = off;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET escape_string_warning = off;

--
-- Name: SCHEMA public; Type: COMMENT; Schema: -; Owner: postgres
--

COMMENT ON SCHEMA public IS 'Standard public schema';


SET search_path = public, pg_catalog;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- Name: testtable; Type: TABLE; Schema: public; Owner: postgres; Tablespace:
--

CREATE TABLE testtable (
    id bigint,
    name character varying(30)
);


ALTER TABLE public.testtable OWNER TO postgres;

--
-- Name: testview; Type: VIEW; Schema: public; Owner: postgres
--

CREATE VIEW testview WITH (security_barrier) AS
    SELECT testtable.id, testtable.name FROM testtable;


ALTER TABLE public.testview OWNER TO postgres;

--
-- Data for Name: testtable; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- Name: public; Type: ACL; Schema: -; Owner: postgres
--

REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM postgres;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO PUBLIC;


--
-- PostgreSQL database dump complete
--

