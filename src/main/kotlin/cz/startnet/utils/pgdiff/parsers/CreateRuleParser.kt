package cz.startnet.utils.pgdiff.parsers

import cz.startnet.utils.pgdiff.schema.PgRule
import cz.startnet.utils.pgdiff.schema.toQualifiedName

/**
CREATE [ OR REPLACE ] RULE name AS ON event
TO table_name [ WHERE condition ]
DO [ ALSO | INSTEAD ] { NOTHING | command | ( command ; command ... ) }
 */
object CreateRuleParser : PatternBasedSubParser(
    "^CREATE[\\s]+RULE[\\s]+.*$"
) {
    override fun parse(parser: Parser, ctx: ParserContext) {
        parser.expect("CREATE")
        parser.expectOptional("OR", "REPLACE")
        parser.expect("RULE")
        val ruleName = parser.parseIdentifier()
        parser.expect("AS", "ON")
        val event = if (parser.expectOptional("INSERT")) {
            "INSERT"
        } else if (parser.expectOptional("UPDATE")) {
            "UPDATE"
        } else if (parser.expectOptional("DELETE")) {
            "DELETE"
        } else if (parser.expectOptional("SELECT")) {
            "SELECT"
        } else {
            error("rule event cannot be parsed from ${parser.rest}")
        }
        parser.expect("TO")
        val relationName = parser.parseIdentifier().toQualifiedName(ctx.database.defaultSchema.name)
        val query = parser.rest
        val relationSchema = ctx.database.getSchema(relationName.schema)
            ?: error("cannot resolve schema for rule relation $relationName  in stmt: ${parser.string}")

        val relation = relationSchema.getRelation(relationName.name)
            ?: error("cannot resolve relation for rule $relationName in stmt: ${parser.string}")
        val rule = PgRule(ParserUtils.getObjectName(ruleName), relationName, event, query)
        relation.addRule(rule)
    }
}