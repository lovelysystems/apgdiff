package cz.startnet.utils.pgdiff.parsers

import cz.startnet.utils.pgdiff.schema.PgPolicy

object CreatePolicyParser : PatternBasedSubParser(
    "^CREATE[\\s]+POLICY[\\s]+.*$"
) {
    override fun parse(parser: Parser, ctx: ParserContext) {
        parser.expect("CREATE", "POLICY")
        val policyName = parser.parseIdentifier()
        val policy = PgPolicy(policyName)
        parser.expect("ON")
        val qualifiedTableName = parser.parseIdentifier()
        val schemaName = ParserUtils.getSchemaName(qualifiedTableName, ctx.database)
        val schema = parser.withErrorContext {
            ctx.database.getSchemaSafe(schemaName)
        }
        val table = schema.getTableSafe(ParserUtils.getObjectName(qualifiedTableName))
        if (parser.expectOptional("FOR")) {
            val command = parser.expectOptionalOneOf(
                "ALL", "SELECT",
                "INSERT", "UPDATE", "DELETE"
            )
            policy.command = command
        } else {
            policy.command = "ALL"
        }
        if (parser.expectOptional("TO")) {
            if (parser.expectOptional("PUBLIC")) {
                policy.roles.add("PUBLIC")
            } else {
                var role: String? = parser.parseIdentifier()
                policy.roles.add(role!!)
                while (role != null) {
                    if (parser.expectOptional(",")) {
                        parser.skipWhitespace()
                        role = parser.parseIdentifier()
                        policy.roles.add(role)
                    } else {
                        role = null
                    }
                }
            }
        } else {
            policy.roles.add("PUBLIC")
        }
        if (parser.expectOptional("USING")) {
            parser.expect("(")
            policy.using = parser.expression
            parser.expect(")")
        }
        if (parser.expectOptional("WITH", "CHECK")) {
            parser.expect("(")
            policy.withCheck = parser.expression
            parser.expect(")")
        }
        policy.tableName = table.name
        table.addPolicy(policy)
    }
}