/**
 * Copyright 2018 StartNet s.r.o.
 *
 * Distributed under MIT license
 */
package cz.startnet.utils.pgdiff.parsers

import cz.startnet.utils.pgdiff.Resources
import cz.startnet.utils.pgdiff.schema.PgRule
import java.text.MessageFormat

/**
 * Parses CREATE RULE statements.
 *
 * @author jalissonmello
 */
object CreateRuleParser : PatternBasedSubParser(
    "^CREATE[\\s]+RULE[\\s]+.*$"
) {
    override fun parse(parser: Parser, ctx: ParserContext) {
        parser.expect("CREATE")
        parser.expectOptional("OR", "REPLACE")
        parser.expect("RULE")
        val ruleName = parser.parseIdentifier()
        val rule = PgRule(ParserUtils.getObjectName(ruleName))
        parser.expect("AS", "ON")
        if (parser.expectOptional("INSERT")) {
            rule.event = "INSERT"
        } else if (parser.expectOptional("UPDATE")) {
            rule.event = "UPDATE"
        } else if (parser.expectOptional("DELETE")) {
            rule.event = "DELETE"
        } else if (parser.expectOptional("SELECT")) {
            rule.event = "SELECT"
        } else {
            parser.throwUnsupportedCommand()
        }
        parser.expect("TO")
        val relationName = parser.parseIdentifier()
        val query = parser.rest
        rule.relationName = ParserUtils.getObjectName(relationName)
        rule.query = query
        val schemaName = ParserUtils.getSchemaName(ruleName, ctx.database)
        val schema = ctx.database.getSchema(schemaName)
            ?: throw RuntimeException(
                MessageFormat.format(
                    Resources.getString("CannotFindSchema"), schemaName,
                    parser.string
                )
            )
        schema.getRelation(rule.relationName)!!.addRule(rule)
    }
}