/**
 * Copyright 2010 StartNet s.r.o.
 */
package cz.startnet.utils.pgdiff.parsers

import cz.startnet.utils.pgdiff.Resources
import cz.startnet.utils.pgdiff.schema.PgDatabase
import cz.startnet.utils.pgdiff.schema.PgFunction
import java.text.MessageFormat

/**
 * COMMENT parser.
 *
 * @author fordfrog
 */
object CommentParser {
    /**
     * Parses COMMENT statements.
     *
     * @param database                database
     * @param statement               COMMENT statement
     * @param outputIgnoredStatements whether ignored statements should be
     * output into the diff
     */
    fun parse(
        database: PgDatabase,
        statement: String, outputIgnoredStatements: Boolean
    ) {
        val parser = Parser(statement)
        parser.expect("COMMENT", "ON")
        if (parser.expectOptional("TABLE")) {
            parseTable(parser, database)
        } else if (parser.expectOptional("COLUMN")) {
            parseColumn(parser, database)
        } else if (parser.expectOptional("CONSTRAINT")) {
            parseConstraint(parser, database)
        } else if (parser.expectOptional("DATABASE")) {
            parseDatabase(parser, database)
        } else if (parser.expectOptional("FUNCTION")) {
            parseFunction(parser, database)
        } else if (parser.expectOptional("INDEX")) {
            parseIndex(parser, database)
        } else if (parser.expectOptional("SCHEMA")) {
            parseSchema(parser, database)
        } else if (parser.expectOptional("SEQUENCE")) {
            parseSequence(parser, database)
        } else if (parser.expectOptional("TRIGGER")) {
            parseTrigger(parser, database)
        } else if (parser.expectOptional("VIEW")) {
            parseView(parser, database)
        } else if (outputIgnoredStatements) {
            database.addIgnoredStatement(statement)
        }
    }

    /**
     * Parses COMMENT ON TABLE.
     *
     * @param parser   parser
     * @param database database
     */
    private fun parseTable(
        parser: Parser,
        database: PgDatabase
    ) {
        val tableName = parser.parseIdentifier()
        val objectName = ParserUtils.getObjectName(tableName)
        val schemaName = ParserUtils.getSchemaName(tableName, database)
        val table = database.getSchema(schemaName)!!.getTable(objectName)!!
        parser.expect("IS")
        table.comment = getComment(parser)
        parser.expect(";")
    }

    /**
     * Parses COMMENT ON CONSTRAINT.
     *
     * @param parser   parser
     * @param database database
     */
    private fun parseConstraint(
        parser: Parser,
        database: PgDatabase
    ) {
        val constraintName = ParserUtils.getObjectName(parser.parseIdentifier())
        parser.expect("ON")
        val tableName = parser.parseIdentifier()
        val objectName = ParserUtils.getObjectName(tableName)
        val schemaName = ParserUtils.getSchemaName(tableName, database)
        val constraint = database.getSchema(schemaName)!!.getTable(objectName)!!.getConstraint(constraintName)!!
        parser.expect("IS")
        constraint.comment = getComment(parser)
        parser.expect(";")
    }

    /**
     * Parses COMMENT ON DATABASE.
     *
     * @param parser   parser
     * @param database database
     */
    private fun parseDatabase(
        parser: Parser,
        database: PgDatabase
    ) {
        parser.parseIdentifier()
        parser.expect("IS")
        database.comment = getComment(parser)
        parser.expect(";")
    }

    /**
     * Parses COMMENT ON INDEX.
     *
     * @param parser   parser
     * @param database database
     */
    private fun parseIndex(
        parser: Parser,
        database: PgDatabase
    ) {
        val indexName = parser.parseIdentifier()
        val objectName = ParserUtils.getObjectName(indexName)
        val schemaName = ParserUtils.getSchemaName(indexName, database)
        val schema = database.getSchema(schemaName)
        val index = schema!!.getIndex(objectName)
        if (index == null) {
            val primaryKey = schema.getPrimaryKey(objectName)!!
            parser.expect("IS")
            primaryKey.comment = getComment(parser)
            parser.expect(";")
        } else {
            parser.expect("IS")
            index.comment = getComment(parser)
            parser.expect(";")
        }
    }

    /**
     * Parses COMMENT ON SCHEMA.
     *
     * @param parser   parser
     * @param database database
     */
    private fun parseSchema(
        parser: Parser,
        database: PgDatabase
    ) {
        val schemaName = ParserUtils.getObjectName(parser.parseIdentifier())
        val schema = database.getSchema(schemaName)!!
        parser.expect("IS")
        schema.comment = getComment(parser)
        parser.expect(";")
    }

    /**
     * Parses COMMENT ON SEQUENCE.
     *
     * @param parser   parser
     * @param database database
     */
    private fun parseSequence(
        parser: Parser,
        database: PgDatabase
    ) {
        val sequenceName = parser.parseIdentifier()
        val objectName = ParserUtils.getObjectName(sequenceName)
        val schemaName = ParserUtils.getSchemaName(sequenceName, database)
        val sequence = database.getSchema(schemaName)!!.getSequence(objectName)!!
        parser.expect("IS")
        sequence.comment = getComment(parser)
        parser.expect(";")
    }

    /**
     * Parses COMMENT ON TRIGGER.
     *
     * @param parser   parser
     * @param database database
     */
    private fun parseTrigger(
        parser: Parser,
        database: PgDatabase
    ) {
        val triggerName = ParserUtils.getObjectName(parser.parseIdentifier())
        parser.expect("ON")
        val tableName = parser.parseIdentifier()
        val objectName = ParserUtils.getObjectName(tableName)
        val schemaName = ParserUtils.getSchemaName(tableName, database)
        val trigger = database.getSchema(schemaName)!!.getTable(objectName)!!.getTrigger(triggerName)!!
        parser.expect("IS")
        trigger.comment = getComment(parser)
        parser.expect(";")
    }

    /**
     * Parses COMMENT ON VIEW.
     *
     * @param parser   parser
     * @param database database
     */
    private fun parseView(
        parser: Parser,
        database: PgDatabase
    ) {
        val viewName = parser.parseIdentifier()
        val objectName = ParserUtils.getObjectName(viewName)
        val schemaName = ParserUtils.getSchemaName(viewName, database)
        val view = database.getSchema(schemaName)!!.getView(objectName)!!
        parser.expect("IS")
        view.comment = getComment(parser)
        parser.expect(";")
    }

    /**
     * Parses COMMENT ON COLUMN.
     *
     * @param parser   parser
     * @param database database
     */
    private fun parseColumn(
        parser: Parser,
        database: PgDatabase
    ) {
        val columnName = parser.parseIdentifier()
        val objectName = ParserUtils.getObjectName(columnName)
        val relName = ParserUtils.getSecondObjectName(columnName)
        val schemaName = ParserUtils.getThirdObjectName(columnName)
        val schema = database.getSchema(schemaName)
        val rel = schema!!.getRelation(relName)
        val column = rel!!.getColumn(objectName)
                ?: throw ParserException(
                    MessageFormat.format(
                        Resources.getString("CannotFindColumnInTable"),
                        columnName, rel.name
                    )
                )
        parser.expect("IS")
        column.comment = getComment(parser)
        parser.expect(";")
    }

    /**
     * Parses COMMENT ON FUNCTION.
     *
     * @param parser   parser
     * @param database database
     */
    private fun parseFunction(
        parser: Parser,
        database: PgDatabase
    ) {
        val functionName = parser.parseIdentifier()
        val objectName = ParserUtils.getObjectName(functionName)
        val schemaName = ParserUtils.getSchemaName(functionName, database)
        val schema = database.getSchema(schemaName)
        parser.expect("(")
        val tmpFunction = PgFunction()
        tmpFunction.name = objectName
        while (!parser.expectOptional(")")) {
            val mode: String?
            mode = if (parser.expectOptional("IN")) {
                "IN"
            } else if (parser.expectOptional("OUT")) {
                "OUT"
            } else if (parser.expectOptional("INOUT")) {
                "INOUT"
            } else if (parser.expectOptional("VARIADIC")) {
                "VARIADIC"
            } else {
                null
            }
            val position = parser.position
            var argumentName: String? = null
            var dataType = parser.parseDataType()
            val position2 = parser.position
            if (!parser.expectOptional(")") && !parser.expectOptional(",")) {
                parser.position = position
                argumentName = ParserUtils.getObjectName(parser.parseIdentifier())
                dataType = parser.parseDataType()
            } else {
                parser.position = position2
            }
            val argument = PgFunction.Argument()
            argument.dataType = dataType
            argument.mode = mode
            argument.name = argumentName
            tmpFunction.addArgument(argument)
            if (parser.expectOptional(")")) {
                break
            } else {
                parser.expect(",")
            }
        }
        val function = schema!!.getFunction(tmpFunction.signature)!!
        parser.expect("IS")
        function.comment = getComment(parser)
        parser.expect(";")
    }

    /**
     * Parses comment from parser. If comment is "null" string then null is
     * returned, otherwise the parsed string is returned.
     *
     * @param parser parser
     *
     * @return string or null
     */
    private fun getComment(parser: Parser): String? {
        val comment = parser.parseString()
        return if ("null".equals(comment, ignoreCase = true)) {
            null
        } else comment
    }
}