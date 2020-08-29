package cz.startnet.utils.pgdiff.parsers

object DefaultSchemaParser : PatternBasedSubParser(
    "^SET[\\s]+search_path[\\s]*=[\\s]*\"?([^,\\s\"]+)\"?"
            + "(?:,[\\s]+.*)?;$"
) {
    override fun parse(parser: Parser, ctx: ParserContext) {
        val matcher = pattern.matcher(parser.string)
        matcher.matches()
        ctx.database.setDefaultSchema(matcher.group(1)!!)
    }
}