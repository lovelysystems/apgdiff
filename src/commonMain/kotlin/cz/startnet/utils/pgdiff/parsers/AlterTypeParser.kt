package cz.startnet.utils.pgdiff.parsers


/**
 * ALTER TYPE name OWNER TO new_owner
 */
object AlterTypeParser : PatternBasedSubParser(
    "^ALTER[\\s]+TYPE[\\s]+.*[\\s]+OWNER[\\s]+TO[\\s]+.*$"
) {
    override fun parse(parser: Parser, ctx: ParserContext) {
        parser.expect("ALTER", "TYPE")
        val objectName = ctx.database.getSchemaObjectName(parser.parseIdentifier())
        val schema = ctx.database.getSchema(objectName)
        val type = schema.getType(objectName.name)
            ?: error("type $objectName not found")
        parser.expect("OWNER", "TO")
        type.owner = parser.parseIdentifier()
        require(parser.rest.isNullOrEmpty())
    }
}