package cz.startnet.utils.pgdiff.parsers

import cz.startnet.utils.pgdiff.schema.PgFunction
import cz.startnet.utils.pgdiff.schema.PgFunctionArgument


/**
 * Parses a function signature and returns a PGFunction
 * name ( [ [ argmode ] [ argname ] argtype [ { DEFAULT | = } default_expr ] [, ...] ] )
 */
fun Parser.parseFunctionSignature(ctx: ParserContext): PgFunction {
    val functionName = parseIdentifier()
    val schemaName = ctx.database.getSchemaName(functionName)
    val objectName = ParserUtils.getObjectName(functionName)
    val func = PgFunction(objectName, schemaName)
    parseFunctionArguments(func::addArgument)
    return func
}

fun Parser.parseFunctionArguments(receiver: (PgFunctionArgument) -> Unit = {}) {
    expect("(")
    while (!expectOptional(")")) {
        val mode: String = when {
            expectOptional("IN") -> {
                "IN"
            }
            expectOptional("OUT") -> {
                "OUT"
            }
            expectOptional("INOUT") -> {
                "INOUT"
            }
            expectOptional("VARIADIC") -> {
                "VARIADIC"
            }
            else -> {
                "IN"
            }
        }
        val posPreType = this.position
        var argumentName: String? = null
        var dataType = parseDataType()
        val posPostType = this.position
        if (!expectOptional(")") && !expectOptional(",")) {
            this.position = posPreType
            argumentName = ParserUtils.getObjectName(parseIdentifier())
            dataType = parseDataType()
        } else {
            this.position = posPostType
        }
        val defaultExpression = if (expectOptional("DEFAULT")) {
            this.expression
        } else {
            null
        }
        val argument = PgFunctionArgument(mode, argumentName, dataType, defaultExpression)
        receiver(argument)


        if (expectOptional(")")) {
            break
        } else {
            expect(",")
        }
    }
}


/**
 * Parses CREATE FUNCTION and CREATE OR REPLACE FUNCTION statements.
 *
 * @author fordfrog
 */
object CreateFunctionParser : PatternBasedSubParser(
    "^CREATE[\\s]+(?:OR[\\s]+REPLACE[\\s]+)?FUNCTION[\\s]+.*$"
) {
    /**
     * Parses CREATE FUNCTION and CREATE OR REPLACE FUNCTION statement.
     *
     * @param database  database
     * @param statement CREATE FUNCTION statement
     */
    override fun parse(parser: Parser, ctx: ParserContext) {
        parser.expect("CREATE")
        parser.expectOptional("OR", "REPLACE")
        parser.expect("FUNCTION")

        val function = parser.parseFunctionSignature(ctx)
        val schema = parser.withErrorContext {
            ctx.database.getSchemaSafe(function.schema)
        }
        schema.addFunction(function)
        function.body = parser.rest
    }
}