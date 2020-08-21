/**
 * Copyright 2006 StartNet s.r.o.
 *
 * Distributed under MIT license
 */
package cz.startnet.utils.pgdiff.parsers

import cz.startnet.utils.pgdiff.Resources
import cz.startnet.utils.pgdiff.schema.PgDatabase
import cz.startnet.utils.pgdiff.schema.PgFunction
import java.text.MessageFormat

/**
 * Parses CREATE FUNCTION and CREATE OR REPLACE FUNCTION statements.
 *
 * @author fordfrog
 */
object CreateFunctionParser {
    /**
     * Parses CREATE FUNCTION and CREATE OR REPLACE FUNCTION statement.
     *
     * @param database  database
     * @param statement CREATE FUNCTION statement
     */
    fun parse(
        database: PgDatabase,
        statement: String
    ) {
        val parser = Parser(statement)
        parser.expect("CREATE")
        parser.expectOptional("OR", "REPLACE")
        parser.expect("FUNCTION")
        val functionName = parser.parseIdentifier()
        val schemaName = ParserUtils.getSchemaName(functionName, database)
        val schema = database.getSchema(schemaName)
                ?: throw RuntimeException(
                    MessageFormat.format(
                        Resources.getString("CannotFindSchema"), schemaName,
                        statement
                    )
                )
        val function = PgFunction()
        function.name = ParserUtils.getObjectName(functionName)
        schema.addFunction(function)
        parser.expect("(")
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
            if (!parser.expectOptional(")") && !parser.expectOptional(",")
                && !parser.expectOptional("=")
                && !parser.expectOptional("DEFAULT")
            ) {
                parser.position = position
                argumentName = ParserUtils.getObjectName(parser.parseIdentifier())
                dataType = parser.parseDataType()
            } else {
                parser.position = position2
            }
            val defaultExpression: String?
            defaultExpression = if (parser.expectOptional("=")
                || parser.expectOptional("DEFAULT")
            ) {
                parser.expression
            } else {
                null
            }
            val argument = PgFunction.Argument()
            argument.dataType = dataType
            argument.defaultExpression = defaultExpression
            argument.mode = mode
            argument.name = argumentName
            function.addArgument(argument)
            if (parser.expectOptional(")")) {
                break
            } else {
                parser.expect(",")
            }
        }
        function.body = parser.rest
    }
}