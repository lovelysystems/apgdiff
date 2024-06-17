package cz.startnet.utils.pgdiff.parsers


object AlterSchemaParser : PatternBasedSubParser(
    "^ALTER[\\s]+SCHEMA[\\s]+.*[\\s]+OWNER[\\s]+TO[\\s]+.*$"
) {

    override fun parse(parser: Parser, ctx: ParserContext) {
        parser.expect("ALTER", "SCHEMA")
        val schemaName = ParserUtils.getObjectName(parser.parseIdentifier())
        val schema = ctx.database.getSchema(schemaName)
            ?: error("schema $schemaName not found")
        parser.expect("OWNER", "TO")
        schema.owner = parser.parseIdentifier()
        require(parser.rest.isNullOrEmpty())
    }

}