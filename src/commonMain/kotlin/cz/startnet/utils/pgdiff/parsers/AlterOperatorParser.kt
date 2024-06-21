package cz.startnet.utils.pgdiff.parsers


/**
 * ALTER OPERATOR name ( { left_type | NONE } , { right_type | NONE } ) OWNER TO { new_owner | CURRENT_USER | SESSION_USER }
 */
object AlterOperatorParser : PatternBasedSubParser(
    "^ALTER[\\s]+OPERATOR[\\s]+.*[\\s]+OWNER[\\s]+TO[\\s]+.*$"
) {
    override fun parse(parser: Parser, ctx: ParserContext) {
        parser.expect("ALTER", "OPERATOR")
        val operator = parser.parseOperatorSignature(ctx)
        parser.expect("OWNER", "TO")
        operator.owner = parser.parseIdentifier()
    }
}