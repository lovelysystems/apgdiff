package cz.startnet.utils.pgdiff.parsers

import cz.startnet.utils.pgdiff.schema.PgExtension
import cz.startnet.utils.pgdiff.schema.PgSchema

object CreateExtensionParser : PatternBasedSubParser(
    "^CREATE[\\s]+EXTENSION[\\s]+.*$"
) {
    override fun parse(parser: Parser, ctx: ParserContext) {
        parser.expect("CREATE", "EXTENSION")
        parser.expectOptional("IF", "NOT", "EXISTS")
        val extensionName = parser.parseIdentifier()
        val extension = PgExtension(extensionName)
        parser.expectOptional("WITH")
        if (parser.expectOptional("SCHEMA")) {
            extension.schema = PgSchema(parser.parseString())
        }
        if (parser.expectOptional("VERSION")) {
            extension.version = parser.parseString()
        }
        if (parser.expectOptional("FROM")) {
            extension.from = parser.parseString()
        }
        ctx.database.addExtension(extension)
    }
}