package cz.startnet.utils.pgdiff.parsers

import cz.startnet.utils.pgdiff.schema.PgOperator

private fun Parser.optionalDefinition(name: String): String? {
    if (expectOptional(name, "=")) {
        val result = parseIdentifier()
        expectOptional(",")
        return result
    }
    return null
}

/**
 * Parses an operator signature and finds the according PGOperator
 */
fun Parser.parseOperatorSignature(ctx: ParserContext): PgOperator {
    val objectName = ctx.database.getSchemaObjectName(parseIdentifier())
    val schema = ctx.database.getSchema(objectName)
    val tmpOperator = PgOperator(objectName.name)
    expect("(")
    tmpOperator.leftType = parseDataType()
    expect(",")
    tmpOperator.rightType = parseDataType()
    val operator = schema.getOperator(tmpOperator.signature) ?: error("operator not found ${tmpOperator.signature}")
    expect(")")
    return operator
}

/*
https://www.postgresql.org/docs/12/sql-createoperator.html
CREATE OPERATOR name (
{FUNCTION|PROCEDURE} = function_name
[, LEFTARG = left_type ] [, RIGHTARG = right_type ]
[, COMMUTATOR = com_op ] [, NEGATOR = neg_op ]
[, RESTRICT = res_proc ] [, JOIN = join_proc ]
[, HASHES ] [, MERGES ]
)
*/
object CreateOperatorParser : PatternBasedSubParser(
    "^CREATE[\\s]+OPERATOR[\\s]+.*$"
) {


    override fun parse(parser: Parser, ctx: ParserContext) {
        parser.expect("CREATE", "OPERATOR")
        val objectName = ctx.database.getSchemaObjectName(parser.parseIdentifier())
        val schema = ctx.database.getSchema(objectName)
        val operator = PgOperator(objectName.name)
        schema.operators.add(operator)
        parser.expect("(")
        parser.expect("FUNCTION", "=")
        operator.functionName = parser.parseIdentifier()
        parser.expect(",")

        operator.leftType = parser.optionalDefinition("LEFTARG")
        operator.rightType = parser.optionalDefinition("RIGHTARG")

        if (parser.expectOptional("COMMUTATOR", "=")) {
            operator.comOp = parser.expression
        }
        if (parser.expectOptional("NEGATOR", "=")) {
            operator.negOp = parser.expression
        }
        operator.resProc = parser.optionalDefinition("RESTRICT")
        operator.joinProc = parser.optionalDefinition("JOIN")
        if (parser.expectOptional("HASHES")) {
            operator.hashes = true
            parser.expectOptional(",")
        }
        if (parser.expectOptional("MERGES")) {
            operator.merges = true
            parser.expectOptional(",")
        }
        parser.expect(")")


    }
}
