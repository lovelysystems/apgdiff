CREATE DOMAIN public.benefits AS text
    CONSTRAINT benefits_check CHECK (length(VALUE)>1);

-- new domain
CREATE DOMAIN sha1_digest AS text
    CONSTRAINT sha1_digest_check CHECK ((VALUE ~ '^[0-9a-f]{40}$'::text));

COMMENT ON domain public.benefits is 'benefits comment';

ALTER domain sha1_digest OWNER TO admin;