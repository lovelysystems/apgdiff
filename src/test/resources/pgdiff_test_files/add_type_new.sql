CREATE TYPE bug_status AS ENUM (
    'new',
    'open',
    'closed'
);


ALTER TYPE bug_status OWNER TO postgres;

--
-- TOC entry 549 (class 1247 OID 16389)
-- Name: descr_type; Type: TYPE; Schema: public; Owner: dv
--

CREATE TYPE descr_type AS (
	name text,
	amount integer
);


ALTER TYPE descr_type OWNER TO postgres;

--
-- TOC entry 182 (class 1259 OID 16390)
-- Name: t1; Type: TABLE; Schema: public; Owner: dv
--

CREATE TABLE t1 (
    id integer,
    descr descr_type
);


ALTER TABLE t1 OWNER TO postgres;

