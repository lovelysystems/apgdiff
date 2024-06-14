package cz.startnet.utils.pgdiff.parsers


/**
 * ALTER FUNCTION name [ ( [ [ argmode ] [ argname ] argtype [, ...] ] ) ]OWNER TO new_owner
 */
object AlterFunctionParser : PatternBasedSubParser(
    "^ALTER[\\s]+FUNCTION[\\s]+.*[\\s]+OWNER[\\s]+TO[\\s]+.*$"
) {
    override fun parse(parser: Parser, ctx: ParserContext) {
        parser.expect("ALTER", "FUNCTION")
        val tmpFunction = parser.parseFunctionSignature(ctx)
        val schema = ctx.database.getSchema(tmpFunction.schema)
        val function = schema!!.getFunction(tmpFunction.signature)!!
        parser.expect("OWNER", "TO")
        function.owner = parser.parseIdentifier()
    }
}