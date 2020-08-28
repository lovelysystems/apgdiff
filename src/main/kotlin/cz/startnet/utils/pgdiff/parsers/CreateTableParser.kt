package cz.startnet.utils.pgdiff.parsers

import cz.startnet.utils.pgdiff.Resources
import cz.startnet.utils.pgdiff.schema.*
import java.text.MessageFormat

object CreateTableParser : PatternBasedSubParser(
    "^CREATE[\\s]+(UNLOGGED\\s|FOREIGN\\s)*TABLE[\\s]+.*$"
) {
    override fun parse(parser: Parser, ctx: ParserContext) {
        parser.expect("CREATE")
        val unlogged = parser.expectOptional("UNLOGGED")
        val foreign = parser.expectOptional("FOREIGN")
        parser.expect("TABLE")

        // Optional IF NOT EXISTS, irrelevant for our purposes
        parser.expectOptional("IF", "NOT", "EXISTS")
        val tableName = parser.parseIdentifier()
        val schemaName = ParserUtils.getSchemaName(tableName, ctx.database)
        var schema = ctx.database.getSchema(schemaName)
        if (schema == null) {
            if (ctx.ignoreSchemaCreation) {
                schema = PgSchema(schemaName)
                ctx.database.addSchema(schema)
            } else {
                throw RuntimeException(
                    MessageFormat.format(
                        Resources.getString("CannotFindSchema"), schemaName,
                        parser.string
                    )
                )
            }
        }
        val table = PgTable(ParserUtils.getObjectName(tableName), ctx.database, schema)
        table.isUnlogged = unlogged
        table.isForeign = foreign
        schema.addRelation(table)
        parser.expect("(")
        while (!parser.expectOptional(")")) {
            if (parser.expectOptional("CONSTRAINT")) {
                parseConstraint(parser, table)
            } else if (parser.expectOptional("PRIMARY", "KEY")) {
                throw ParserException(
                    Resources.getString(
                        "CreateTablePrimaryKeyNotSupported"
                    )
                )
            } else if (parser.expectOptional("UNIQUE")) {
                throw ParserException(
                    Resources.getString("CreateTableUniqueNotSupported")
                )
            } else {
                parseColumn(parser, table)
            }
            if (parser.expectOptional(")")) {
                break
            } else {
                parser.expect(",")
            }
        }
        while (!parser.expectOptional(";")) {
            if (parser.expectOptional("INHERITS")) {
                parseInherits(ctx.database, parser, table)
            } else if (parser.expectOptional("WITHOUT")) {
                table.with = "OIDS=false"
            } else if (parser.expectOptional("WITH")) {
                if (parser.expectOptional("OIDS")
                    || parser.expectOptional("OIDS=true")
                ) {
                    table.with = "OIDS=true"
                } else if (parser.expectOptional("OIDS=false")) {
                    table.with = "OIDS=false"
                } else {
                    table.with = parser.expression
                }
            } else if (parser.expectOptional("TABLESPACE")) {
                table.tablespace = parser.parseString()
            } else if (parser.expectOptional("SERVER")) {
                table.foreignServer = parser.expression
            } else if (parser.expectOptional("PARTITION", "BY", "RANGE")) {
                table.rangePartition = parser.expression
            } else {
                parser.throwUnsupportedCommand()
            }
        }
    }

    /**
     * Parses INHERITS.
     *
     * @param ctx.database ctx.database
     * @param parser parser
     * @param table  pg table
     */
    private fun parseInherits(
        database: PgDatabase, parser: Parser,
        table: PgTable
    ) {
        parser.expect("(")
        while (!parser.expectOptional(")")) {
            val parsedString = parser.parseIdentifier()
            val tableName = ParserUtils.getObjectName(parsedString)
            val schemaName =
                if (parsedString.contains(".")) ParserUtils.getSecondObjectName(parsedString) else database.defaultSchema.name
            table.addInherits(schemaName, tableName)
            if (parser.expectOptional(")")) {
                break
            } else {
                parser.expect(",")
            }
        }
    }

    /**
     * Parses CONSTRAINT definition.
     *
     * @param parser parser
     * @param table  table
     */
    private fun parseConstraint(
        parser: Parser,
        table: PgTable
    ) {
        val constraint = PgConstraint(
            ParserUtils.getObjectName(parser.parseIdentifier())
        )
        table.addConstraint(constraint)
        constraint.definition = parser.expression
        constraint.tableName = table.name
    }

    /**
     * Parses column definition.
     *
     * @param parser parser
     * @param table  table
     */
    private fun parseColumn(parser: Parser, table: PgTable) {
        val column = PgColumn(
            ParserUtils.getObjectName(parser.parseIdentifier())
        )
        table.addColumn(column)
        column.parseDefinition(parser.expression)
    }
}