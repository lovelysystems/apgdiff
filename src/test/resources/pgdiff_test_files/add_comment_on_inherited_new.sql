CREATE TABLE article_properties
(
    a text
);

COMMENT ON COLUMN article_properties.a IS 'comment on parent';

CREATE TABLE article_drafts
(
)
    INHERITS (article_properties);

COMMENT ON COLUMN article_drafts.a IS 'comment on inherited';
