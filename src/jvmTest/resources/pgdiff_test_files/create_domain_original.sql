CREATE DOMAIN public.benefits AS text NOT NULL
    CONSTRAINT benefits_check CHECK (length(VALUE)>1);
