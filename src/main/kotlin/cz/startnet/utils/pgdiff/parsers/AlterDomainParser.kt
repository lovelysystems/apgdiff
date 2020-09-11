package cz.startnet.utils.pgdiff.parsers


/**
 * ALTER TYPE name OWNER TO new_owner
 */
object AlterDomainParser : PatternBasedSubParser(
    "^ALTER[\\s]+DOMAIN[\\s]+.*[\\s]+OWNER[\\s]+TO[\\s]+.*$"
) {
    override fun parse(parser: Parser, ctx: ParserContext) {
        parser.expect("ALTER", "DOMAIN")
        val objectName = ctx.database.getSchemaObjectName(parser.parseIdentifier())
        val schema = ctx.database.getSchema(objectName)
        val domain = schema.domains.get(objectName.name)
            ?: error("domain $objectName not found")
        parser.expect("OWNER", "TO")
        domain.owner = parser.parseIdentifier()
        assert(parser.rest.isNullOrEmpty())
    }
}