
SET search_path = public, pg_catalog;
COMMENT ON DOMAIN benefits IS 'benefits comment';
ALTER DOMAIN benefits DROP NOT NULL;

CREATE DOMAIN sha1_digest AS text
    CONSTRAINT sha1_digest_check CHECK ((VALUE ~ '^[0-9a-f]{40}$'::text));
ALTER DOMAIN sha1_digest OWNER TO admin;
