/**
 * Copyright 2006 StartNet s.r.o.
 *
 * Distributed under MIT license
 */
package cz.startnet.utils.pgdiff.parsers

import cz.startnet.utils.pgdiff.Resources
import cz.startnet.utils.pgdiff.schema.*
import java.text.MessageFormat
import java.util.*

/**
 * Parses ALTER TABLE statements.
 *
 * @author fordfrog
 */
object AlterRelationParser {
    /**
     * Parses ALTER TABLE statement.
     *
     * @param database                database
     * @param statement               ALTER TABLE statement
     * @param outputIgnoredStatements whether ignored statements should be
     * output in the diff
     */
    fun parse(
        database: PgDatabase,
        statement: String, outputIgnoredStatements: Boolean
    ) {
        val parser = Parser(statement)
        parser.expect("ALTER")

        /*
         * PostgreSQL allows using ALTER TABLE on views as well as other
         * relation types, so we just ignore type here and derive its type from
         * the original CREATE command.
         */parser.expectOptional("FOREIGN")
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
        val schemaName = ParserUtils.getSchemaName(relName, database)
        val schema = database.getSchema(schemaName)
            ?: throw RuntimeException(
                MessageFormat.format(
                    Resources.getString("CannotFindSchema"), schemaName,
                    statement
                )
            )
        val objectName = ParserUtils.getObjectName(relName)
        val rel = schema.getRelation(objectName)
        if (rel == null) {
            val sequence = schema.getSequence(objectName)
            if (sequence != null) {
                parseSequence(
                    parser, sequence, outputIgnoredStatements,
                    relName, database
                )
                return
            }
            throw RuntimeException(
                MessageFormat.format(
                    Resources.getString("CannotFindObject"), relName,
                    statement
                )
            )
        }
        var table: PgTable? = null
        if (rel is PgTable) {
            table = rel
        }
        while (!parser.expectOptional(";")) {
            if (parser.expectOptional("ALTER")) {
                parseAlterColumn(parser, rel)
            } else if (parser.expectOptional("CLUSTER", "ON")) {
                rel.clusterIndexName = ParserUtils.getObjectName(parser.parseIdentifier())
            } else if (parser.expectOptional("OWNER", "TO")) {
                rel.ownerTo = parser.parseIdentifier()
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
                    parser, outputIgnoredStatements, relName, database
                )
            } else if (table != null && parser.expectOptional("DISABLE")) {
                parseDisable(
                    parser, outputIgnoredStatements, relName, database
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
     * @param outputIgnoredStatements whether ignored statements should be
     * output in the diff
     * @param tableName               table name as it was specified in the
     * statement
     * @param database                database information
     */
    private fun parseEnable(
        parser: Parser,
        outputIgnoredStatements: Boolean, tableName: String?,
        database: PgDatabase
    ) {
        if (parser.expectOptional("REPLICA")) {
            if (parser.expectOptional("TRIGGER")) {
                if (outputIgnoredStatements) {
                    database.addIgnoredStatement(
                        "ALTER TABLE " + tableName
                                + " ENABLE REPLICA TRIGGER "
                                + parser.parseIdentifier() + ';'
                    )
                } else {
                    parser.parseIdentifier()
                }
            } else if (parser.expectOptional("RULE")) {
                if (outputIgnoredStatements) {
                    database.addIgnoredStatement(
                        "ALTER TABLE " + tableName
                                + " ENABLE REPLICA RULE "
                                + parser.parseIdentifier() + ';'
                    )
                } else {
                    parser.parseIdentifier()
                }
            } else {
                parser.throwUnsupportedCommand()
            }
        } else if (parser.expectOptional("ALWAYS")) {
            if (parser.expectOptional("TRIGGER")) {
                if (outputIgnoredStatements) {
                    database.addIgnoredStatement(
                        "ALTER TABLE " + tableName
                                + " ENABLE ALWAYS TRIGGER "
                                + parser.parseIdentifier() + ';'
                    )
                } else {
                    parser.parseIdentifier()
                }
            } else if (parser.expectOptional("RULE")) {
                if (outputIgnoredStatements) {
                    database.addIgnoredStatement(
                        "ALTER TABLE " + tableName
                                + " ENABLE RULE " + parser.parseIdentifier() + ';'
                    )
                } else {
                    parser.parseIdentifier()
                }
            } else {
                parser.throwUnsupportedCommand()
            }
        }
    }

    /**
     * Parses DISABLE statements.
     *
     * @param parser                  parser
     * @param outputIgnoredStatements whether ignored statements should be
     * output in the diff
     * @param tableName               table name as it was specified in the
     * statement
     * @param database                database information
     */
    private fun parseDisable(
        parser: Parser,
        outputIgnoredStatements: Boolean, tableName: String?,
        database: PgDatabase
    ) {
        if (parser.expectOptional("TRIGGER")) {
            if (outputIgnoredStatements) {
                database.addIgnoredStatement(
                    "ALTER TABLE " + tableName
                            + " DISABLE TRIGGER " + parser.parseIdentifier() + ';'
                )
            } else {
                parser.parseIdentifier()
            }
        } else if (parser.expectOptional("RULE")) {
            if (outputIgnoredStatements) {
                database.addIgnoredStatement(
                    "ALTER TABLE " + tableName
                            + " DISABLE RULE " + parser.parseIdentifier() + ';'
                )
            } else {
                parser.parseIdentifier()
            }
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
        rel: PgRelation
    ) {
        parser.expectOptional("COLUMN")
        val columnName = ParserUtils.getObjectName(parser.parseIdentifier())
        if (parser.expectOptional("SET")) {
            if (parser.expectOptional("STATISTICS")) {
                val column = rel.getColumn(columnName)
                    ?: throw RuntimeException(
                        MessageFormat.format(
                            Resources.getString("CannotFindTableColumn"),
                            columnName, rel.name, parser.string
                        )
                    )
                column.statistics = parser.parseInteger()
            } else if (parser.expectOptional("NOT NULL")) {
                if (rel.containsColumn(columnName)) {
                    val column = rel.getColumn(columnName)
                        ?: throw RuntimeException(
                            MessageFormat.format(
                                Resources.getString("CannotFindTableColumn"),
                                columnName, rel.name, parser.string
                            )
                        )
                    column.nullValue = false
                } else if (rel.containsInheritedColumn(columnName)) {
                    val inheritedColumn = rel.getInheritedColumn(columnName)
                        ?: throw RuntimeException(
                            MessageFormat.format(
                                Resources.getString("CannotFindTableColumn"),
                                columnName, rel.name, parser.string
                            )
                        )
                    inheritedColumn.nullValue = false
                } else {
                    throw ParserException(
                        MessageFormat.format(
                            Resources.getString("CannotFindColumnInTable"),
                            columnName, rel.name
                        )
                    )
                }
            } else if (parser.expectOptional("DEFAULT")) {
                val defaultValue = parser.expression
                if (rel.containsColumn(columnName)) {
                    val column = rel.getColumn(columnName)
                        ?: throw RuntimeException(
                            MessageFormat.format(
                                Resources.getString("CannotFindTableColumn"),
                                columnName, rel.name,
                                parser.string
                            )
                        )
                    column.defaultValue = defaultValue
                } else if (rel.containsInheritedColumn(columnName)) {
                    val column = rel.getInheritedColumn(columnName)
                        ?: throw RuntimeException(
                            MessageFormat.format(
                                Resources.getString("CannotFindTableColumn"),
                                columnName, rel.name,
                                parser.string
                            )
                        )
                    column.defaultValue = defaultValue
                } else {
                    throw ParserException(
                        MessageFormat.format(
                            Resources.getString("CannotFindColumnInTable"),
                            columnName, rel.name
                        )
                    )
                }
            } else if (parser.expectOptional("STORAGE")) {
                val column = rel.getColumn(columnName)
                    ?: throw RuntimeException(
                        MessageFormat.format(
                            Resources.getString("CannotFindTableColumn"),
                            columnName, rel.name, parser.string
                        )
                    )
                if (parser.expectOptional("PLAIN")) {
                    column.storage = "PLAIN"
                } else if (parser.expectOptional("EXTERNAL")) {
                    column.storage = "EXTERNAL"
                } else if (parser.expectOptional("EXTENDED")) {
                    column.storage = "EXTENDED"
                } else if (parser.expectOptional("MAIN")) {
                    column.storage = "MAIN"
                } else {
                    parser.throwUnsupportedCommand()
                }
            } else {
                parser.throwUnsupportedCommand()
            }
        } else {
            parser.throwUnsupportedCommand()
        }
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

    /**
     * Parses ALTER TABLE sequence.
     *
     * @param parser                  parser
     * @param sequence                sequence
     * @param outputIgnoredStatements whether ignored statements should be
     * output in the diff
     * @param sequenceName            sequence name as it was specified in the
     * statement
     * @param database                database information
     */
    private fun parseSequence(
        parser: Parser,
        sequence: PgSequence, outputIgnoredStatements: Boolean,
        sequenceName: String?, database: PgDatabase
    ) {
        while (!parser.expectOptional(";")) {
            if (parser.expectOptional("OWNER", "TO")) {
                // we do not parse this one so we just consume the identifier
                if (outputIgnoredStatements) {
                    database.addIgnoredStatement(
                        "ALTER TABLE " + sequenceName
                                + " OWNER TO " + parser.parseIdentifier() + ';'
                    )
                } else {
                    parser.parseIdentifier()
                }
            } else {
                parser.throwUnsupportedCommand()
            }
        }
    }
}