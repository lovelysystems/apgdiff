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
-- Name: test_table_trigger(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION test_table_trigger() RETURNS "trigger"
    AS $$
begin
	return NEW;
end;
$$
    LANGUAGE plpgsql;


ALTER FUNCTION public.test_table_trigger() OWNER TO postgres;

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
-- Name: test_table_trigger; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER test_table_trigger
    BEFORE INSERT OR UPDATE OF id ON test_table
    FOR EACH ROW
    EXECUTE PROCEDURE test_table_trigger();


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

