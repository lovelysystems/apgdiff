package cz.startnet.utils.pgdiff.parsers

object DefaultSchemaParser : PatternBasedSubParser(
    "^SET[\\s]+search_path[\\s]*=[\\s]*\"?([^,\\s\"]+)\"?"
            + "(?:,[\\s]+.*)?;$"
) {
    override fun parse(parser: Parser, ctx: ParserContext) {
        val matcher = pattern.matchEntire(parser.string) ?:  error("schema parser does not match")
        ctx.database.setDefaultSchema(matcher.groupValues[1])
    }
}