CREATE DOMAIN public.benefits AS jsonb
    CONSTRAINT benefits_check CHECK (check_microschema('Benefits'::text, VALUE));

-- new domain
CREATE DOMAIN sha1_digest AS text
    CONSTRAINT sha1_digest_check CHECK ((VALUE ~ '^[0-9a-f]{40}$'::text));

COMMENT ON domain public.benefits is 'benefits comment';

ALTER domain sha1_digest OWNER TO admin;