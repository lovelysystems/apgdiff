package cz.startnet.utils.pgdiff.parsers

import cz.startnet.utils.pgdiff.schema.PgSchema


object CreateSchemaParser : PatternBasedSubParser(
    "^CREATE[\\s]+SCHEMA[\\s]+.*$"
) {

    override fun parse(parser: Parser, ctx: ParserContext) {
        parser.expect("CREATE", "SCHEMA")
        parser.expectOptional("IF NOT EXISTS")
        if (parser.expectOptional("AUTHORIZATION")) {
            val schema = PgSchema(
                ParserUtils.getObjectName(parser.parseIdentifier())
            )
            ctx.database.addSchema(schema)
            schema.authorization = schema.name
            val definition = parser.rest
            if (!definition.isNullOrEmpty()) {
                schema.definition = definition
            }
        } else {
            val schema = PgSchema(
                ParserUtils.getObjectName(parser.parseIdentifier())
            )
            ctx.database.addSchema(schema)
            if (parser.expectOptional("AUTHORIZATION")) {
                schema.authorization = ParserUtils.getObjectName(parser.parseIdentifier())
            }
            val definition = parser.rest
            if (!definition.isNullOrEmpty()) {
                schema.definition = definition
            }
        }
    }

}
