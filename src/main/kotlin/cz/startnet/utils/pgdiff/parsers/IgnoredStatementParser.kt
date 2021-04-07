package cz.startnet.utils.pgdiff.parsers

/**
 * A statement parser which just adds the statement to the ignored statements
 */
class IgnoredStatementParser(regex: String) : PatternBasedSubParser(regex) {

    override fun parse(parser: Parser, ctx: ParserContext) {
        ctx.database.ignoredStatements.add(parser.string)
        super.parse(parser, ctx)
    }
}