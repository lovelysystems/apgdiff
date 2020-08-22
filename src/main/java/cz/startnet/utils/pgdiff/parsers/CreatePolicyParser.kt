/**
 * Copyright 2006 StartNet s.r.o.
 *
 * Distributed under MIT license
 */
package cz.startnet.utils.pgdiff.parsers

import cz.startnet.utils.pgdiff.Resources
import cz.startnet.utils.pgdiff.schema.PgDatabase
import cz.startnet.utils.pgdiff.schema.PgPolicy
import java.text.MessageFormat

object CreatePolicyParser {
    fun parse(
        database: PgDatabase,
        statement: String
    ) {
        val parser = Parser(statement)
        val policy = PgPolicy()
        parser.expect("CREATE", "POLICY")
        val policyName = parser.parseIdentifier()
        parser.expect("ON")
        val qualifiedTableName = parser.parseIdentifier()
        val schemaName = ParserUtils.getSchemaName(qualifiedTableName, database)
        val schema = database.getSchema(schemaName)
                ?: throw RuntimeException(
                    MessageFormat.format(
                        Resources.getString("CannotFindSchema"), schemaName,
                        statement
                    )
                )
        val table = schema.getTable(ParserUtils.getObjectName(qualifiedTableName))
                ?: throw RuntimeException(
                    MessageFormat.format(
                        Resources.getString("CannotFindTable"), qualifiedTableName,
                        statement
                    )
                )
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
                var role:String? = parser.parseIdentifier()
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
        policy.name = policyName
        policy.tableName = table.name
        table.addPolicy(policy)
    }
}