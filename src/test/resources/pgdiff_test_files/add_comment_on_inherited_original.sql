CREATE TABLE article_properties
(
    a text
);
COMMENT ON COLUMN article_properties.a IS 'comment on parent';

CREATE TABLE article_drafts
(
)
    INHERITS (article_properties);
