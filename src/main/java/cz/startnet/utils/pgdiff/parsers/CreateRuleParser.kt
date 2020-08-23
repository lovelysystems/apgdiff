/**
 * Copyright 2018 StartNet s.r.o.
 *
 * Distributed under MIT license
 */
package cz.startnet.utils.pgdiff.parsers

import cz.startnet.utils.pgdiff.Resources
import cz.startnet.utils.pgdiff.schema.PgDatabase
import cz.startnet.utils.pgdiff.schema.PgRule
import java.text.MessageFormat

/**
 * Parses CREATE RULE statements.
 *
 * @author jalissonmello
 */
object CreateRuleParser {
    /**
     * Parses CREATE VIEW statement.
     *
     * @param database database
     * @param statement CREATE VIEW statement
     */
    fun parse(
        database: PgDatabase,
        statement: String
    ) {
        val parser = Parser(statement)
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
        val schemaName = ParserUtils.getSchemaName(ruleName, database)
        val schema = database.getSchema(schemaName)
            ?: throw RuntimeException(
                MessageFormat.format(
                    Resources.getString("CannotFindSchema"), schemaName,
                    statement
                )
            )
        schema.addRelation(rule)
        schema.getRelation(rule.relationName)!!.addRule(rule)
    }
}