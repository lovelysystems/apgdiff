CREATE DOMAIN public.benefits AS jsonb NOT NULL
    CONSTRAINT benefits_check CHECK (check_microschema('Benefits'::text, VALUE));
