# Changes for apgdiff

## unreleased

- allow to specify included and excluded schemas
- fixed an issue which caused identity upon table creation to be normal columns
- order schemas in diffs by first occurrence of a relation to solve dependency issues
- fixed rule creation and drop statements
- added --drop-cascade option to generate cascading drops
- optimized diffs by moving more drops to the end of the diff 
- fixed cross schema owner changes on tables and views
- allow to set an output file for the diff

## 2021-11-13 / 0.0.1

- run diff on ignored statements, to ensure that the batch script is valid
- added support for comments on extensions
- added support sequence owner
- added support for operators
- added support for domains
- added support for `COMMENT ON TYPE ...`
- added support for `ALTER TYPE ... OWNER TO ...`
- added support for `ALTER FUNCTION ... OWNER TO ...`
- added support for `ALTER SCHEMA ... OWNER TO ...`
- added support for `ALTER COLUMN ADD GENERATED ... AS IDENTITY`
- rewrite to kotlin
- replaced maven with gradle
- added docker build

## 2020-07-08 / 2.6.10

For prior changes See [the original readme of the forked project
](https://github.com/lovelysystems/apgdiff/blob/d88afa2f960a4939189c780c73a311019d906565/README.md).
