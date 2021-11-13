/**
 * Copyright 2006 StartNet s.r.o.
 *
 * Distributed under MIT license
 */
package cz.startnet.utils.pgdiff.parsers

import cz.startnet.utils.pgdiff.Resources
import cz.startnet.utils.pgdiff.schema.*
import java.text.MessageFormat


/**
 * Parses ALTER TABLE and ALTER VIEW statements.
 */
object AlterRelationParser : PatternBasedSubParser(
    "^ALTER[\\s](FOREIGN)*TABLE[\\s]+.*$",
    "^ALTER[\\s]+(?:MATERIALIZED[\\s]+)?VIEW[\\s]+.*$",
) {
    override fun parse(parser: Parser, ctx: ParserContext) {
        parser.expect("ALTER")

        /*
         * PostgreSQL allows using ALTER TABLE on views as well as other
         * relation types, so we just ignore type here and derive its type from
         * the original CREATE command.
         */
        parser.expectOptional("FOREIGN")
        //OK FOREIGN TABLE
        if (parser.expectOptional("TABLE")) {
            parser.expectOptional("ONLY")
        } else if (parser.expectOptional("MATERIALIZED", "VIEW")
            || parser.expectOptional("VIEW")
        ) {
            // OK, view
        } else {
            parser.throwUnsupportedCommand()
        }
        val relName = parser.parseIdentifier()
        val schemaName = ParserUtils.getSchemaName(relName, ctx.database)
        val schema = ctx.database.getSchema(schemaName)
            ?: throw RuntimeException(
                MessageFormat.format(
                    Resources.getString("CannotFindSchema"), schemaName,
                    parser.string
                )
            )
        val objectName = ParserUtils.getObjectName(relName)
        val rel = schema.getRelation(objectName)
        if (rel == null) {
            val sequence = schema.getSequence(objectName)
            if (sequence != null) {
                // use the sequence parser since for historical reasons it is also ok to use ALTER TABLE with sequences
                AlterSequenceParser.parseAlter(sequence, parser, ctx)
                return
            }
            throw RuntimeException(
                MessageFormat.format(
                    Resources.getString("CannotFindObject"), relName,
                    parser.string
                )
            )
        }
        var table: PgTable? = null
        if (rel is PgTable) {
            table = rel
        }
        while (!parser.expectOptional(";")) {
            if (parser.expectOptional("ALTER")) {
                parseAlterColumn(parser, rel, ctx.database)
            } else if (parser.expectOptional("CLUSTER", "ON")) {
                rel.clusterIndexName = ParserUtils.getObjectName(parser.parseIdentifier())
            } else if (parser.expectOptional("OWNER", "TO")) {
                rel.owner = parser.parseIdentifier()
            } else if (table != null && parser.expectOptional("ADD")) {
                if (parser.expectOptional("FOREIGN", "KEY")) {
                    parseAddForeignKey(parser, table)
                } else if (parser.expectOptional("CONSTRAINT")) {
                    parseAddConstraint(parser, table, schema)
                } else {
                    parser.throwUnsupportedCommand()
                }
            } else if (table != null
                && parser.expectOptional("ENABLE", "ROW", "LEVEL", "SECURITY")
            ) {
                table.setRLSEnabled(true)
            } else if (table != null
                && parser.expectOptional("DISABLE", "ROW", "LEVEL", "SECURITY")
            ) {
                table.setRLSEnabled(false)
            } else if (table != null
                && parser.expectOptional("FORCE", "ROW", "LEVEL", "SECURITY")
            ) {
                table.setRLSForced(true)
            } else if (table != null
                && parser.expectOptional("NO", "FORCE", "ROW", "LEVEL", "SECURITY")
            ) {
                table.setRLSForced(false)
            } else if (table != null && parser.expectOptional("ENABLE")) {
                parseEnable(
                    parser, relName, ctx.database
                )
            } else if (table != null && parser.expectOptional("DISABLE")) {
                parseDisable(
                    parser, relName, ctx.database
                )
            } else {
                parser.throwUnsupportedCommand()
            }
            if (parser.expectOptional(";")) {
                break
            } else {
                parser.expect(",")
            }
        }
    }

    /**
     * Parses ENABLE statements.
     *
     * @param parser                  parser
     * output in the diff
     * @param tableName               table name as it was specified in the
     * statement
     * @param database                database information
     */
    private fun parseEnable(
        parser: Parser,
        tableName: String?, database: PgDatabase
    ) {
        if (parser.expectOptional("REPLICA")) {
            if (parser.expectOptional("TRIGGER")) {
                database.addIgnoredStatement(
                    "ALTER TABLE " + tableName
                            + " ENABLE REPLICA TRIGGER "
                            + parser.parseIdentifier() + ';'
                )
            } else if (parser.expectOptional("RULE")) {
                database.addIgnoredStatement(
                    "ALTER TABLE " + tableName
                            + " ENABLE REPLICA RULE "
                            + parser.parseIdentifier() + ';'
                )
            } else {
                parser.throwUnsupportedCommand()
            }
        } else if (parser.expectOptional("ALWAYS")) {
            if (parser.expectOptional("TRIGGER")) {
                database.addIgnoredStatement(
                    "ALTER TABLE " + tableName
                            + " ENABLE ALWAYS TRIGGER "
                            + parser.parseIdentifier() + ';'
                )
            } else if (parser.expectOptional("RULE")) {
                database.addIgnoredStatement(
                    "ALTER TABLE " + tableName
                            + " ENABLE RULE " + parser.parseIdentifier() + ';'
                )
            } else {
                parser.throwUnsupportedCommand()
            }
        }
    }

    /**
     * Parses DISABLE statements.
     *
     * @param parser                  parser
     * @param tableName               table name as it was specified in the statement
     * @param database                database information
     */
    private fun parseDisable(
        parser: Parser,
        tableName: String?, database: PgDatabase
    ) {
        if (parser.expectOptional("TRIGGER")) {
            database.addIgnoredStatement(
                "ALTER TABLE " + tableName
                        + " DISABLE TRIGGER " + parser.parseIdentifier() + ';'
            )
        } else if (parser.expectOptional("RULE")) {
            database.addIgnoredStatement(
                "ALTER TABLE " + tableName
                        + " DISABLE RULE " + parser.parseIdentifier() + ';'
            )
        } else {
            parser.throwUnsupportedCommand()
        }
    }

    /**
     * Parses ADD CONSTRAINT action.
     *
     * @param parser parser
     * @param table  table
     * @param schema schema
     */
    private fun parseAddConstraint(
        parser: Parser,
        table: PgTable, schema: PgSchema
    ) {
        val constraintName = ParserUtils.getObjectName(parser.parseIdentifier())
        val constraint = PgConstraint(constraintName)
        constraint.tableName = table.name
        table.addConstraint(constraint)
        if (parser.expectOptional("PRIMARY", "KEY")) {
            schema.addPrimaryKey(constraint)
            constraint.definition = "PRIMARY KEY " + parser.expression
        } else {
            constraint.definition = parser.expression
        }
    }

    /**
     * Parses ALTER COLUMN action.
     *
     * @param parser parser
     * @param rel    view/table
     */
    private fun parseAlterColumn(
        parser: Parser,
        rel: PgRelation<*, *>,
        database: PgDatabase
    ) {
        parser.expectOptional("COLUMN")
        val columnName = ParserUtils.getObjectName(parser.parseIdentifier())
        val alterColumnParser =
            AlterColumnParser(columnName, parser, rel, ParserContext(database))
        alterColumnParser.parse()
    }

    /**
     * Parses ADD FOREIGN KEY action.
     *
     * @param parser parser
     * @param table  pg table
     */
    private fun parseAddForeignKey(
        parser: Parser,
        table: PgTable
    ) {
        val columnNames: MutableList<String?> = ArrayList(1)
        parser.expect("(")
        while (!parser.expectOptional(")")) {
            columnNames.add(
                ParserUtils.getObjectName(parser.parseIdentifier())
            )
            if (parser.expectOptional(")")) {
                break
            } else {
                parser.expect(",")
            }
        }
        val constraintName = ParserUtils.generateName(
            table.name + "_", columnNames, "_fkey"
        )
        val constraint = PgConstraint(constraintName)
        table.addConstraint(constraint)
        constraint.definition = parser.expression
        constraint.tableName = table.name
    }
}