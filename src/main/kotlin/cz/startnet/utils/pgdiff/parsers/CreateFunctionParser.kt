package cz.startnet.utils.pgdiff.parsers

import cz.startnet.utils.pgdiff.Resources
import cz.startnet.utils.pgdiff.schema.PgFunction
import java.text.MessageFormat


/**
 * Parses a function signature and returns a PGFunction
 */
fun Parser.parseFunctionSignature(ctx: ParserContext): PgFunction {
    val functionName = parseIdentifier()
    val schemaName = ctx.database.getSchemaName(functionName)
    val objectName = ParserUtils.getObjectName(functionName)
    val func = PgFunction(objectName, schemaName)
    expect("(")
    while (!expectOptional(")")) {
        val mode: String?
        mode = if (expectOptional("IN")) {
            "IN"
        } else if (expectOptional("OUT")) {
            "OUT"
        } else if (expectOptional("INOUT")) {
            "INOUT"
        } else if (expectOptional("VARIADIC")) {
            "VARIADIC"
        } else {
            null
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
        val argument = PgFunction.Argument()
        argument.dataType = dataType
        argument.mode = mode
        argument.name = argumentName
        func.addArgument(argument)
        if (expectOptional(")")) {
            break
        } else {
            expect(",")
        }
    }
    return func
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
        val schema = ctx.database.getSchema(function.schema)
            ?: throw RuntimeException(
                MessageFormat.format(
                    Resources.getString("CannotFindSchema"), function.schema,
                    parser.string
                )
            )
        schema.addFunction(function)
        function.body = parser.rest
    }
}