--
-- PostgreSQL database dump
--

SET client_encoding = 'UTF8';
SET check_function_bodies = false;
SET client_min_messages = warning;

--
-- Name: SCHEMA public; Type: COMMENT; Schema: -; Owner: postgres
--

COMMENT ON SCHEMA public IS 'Standard public schema';




SET search_path = public, pg_catalog;

--
-- Name: return_one(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION return_one() RETURNS integer
    AS $$
begin
	return 1;
end;
$$
    LANGUAGE plpgsql;


ALTER FUNCTION public.return_one() OWNER TO postgres;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- Name: test_table; Type: TABLE; Schema: public; Owner: postgres; Tablespace:
--

CREATE TABLE test_table (
    id serial NOT NULL
);


ALTER TABLE public.test_table OWNER TO postgres;

--
-- Name: test_table_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace:
--

ALTER TABLE ONLY test_table
    ADD CONSTRAINT test_table_pkey PRIMARY KEY (id);


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

