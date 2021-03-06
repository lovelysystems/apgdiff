--
-- Name: inventoryitemsupplier; Type: TABLE; Schema: public; Owner: postgres; Tablespace:
--

CREATE TABLE inventoryitemsupplier (
    id integer NOT NULL,
    code character varying(20) NOT NULL,
    partneridentificationid integer NOT NULL,
    inventoryitemid integer NOT NULL,
    createdbyuserid smallint NOT NULL,
    datecreated timestamp without time zone NOT NULL,
    datedeleted timestamp without time zone,
    datelastmodified timestamp without time zone,
    deletedbyuserid smallint,
    lastmodifiedbyuserid smallint
);


ALTER TABLE public.inventoryitemsupplier OWNER TO postgres;

--
-- Name: inventoryitemsupplier_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE inventoryitemsupplier_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.inventoryitemsupplier_seq OWNER TO postgres;

--
-- Name: inventoryitemsupplier_inventoryitemid_key; Type: INDEX; Schema: public; Owner: postgres; Tablespace:
--

CREATE INDEX inventoryitemsupplier_inventoryitemid_key ON inventoryitemsupplier USING btree (inventoryitemid);


--
-- Name: inventoryitemsupplier_partneridentificationid_key; Type: INDEX; Schema: public; Owner: postgres; Tablespace:
--

CREATE INDEX inventoryitemsupplier_partneridentificationid_key ON inventoryitemsupplier USING btree (partneridentificationid);


