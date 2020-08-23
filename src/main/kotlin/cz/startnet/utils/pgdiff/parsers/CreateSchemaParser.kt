/**
 * Copyright 2006 StartNet s.r.o.
 *
 * Distributed under MIT license
 */
package cz.startnet.utils.pgdiff.parsers

import cz.startnet.utils.pgdiff.schema.PgDatabase
import cz.startnet.utils.pgdiff.schema.PgSchema

/**
 * Parses CREATE SCHEMA statements.
 *
 * @author fordfrog
 */
object CreateSchemaParser {
    /**
     * Parses CREATE SCHEMA statement.
     *
     * @param database  database
     * @param statement CREATE SCHEMA statement
     */
    fun parse(
        database: PgDatabase,
        statement: String
    ) {
        val parser = Parser(statement)
        parser.expect("CREATE", "SCHEMA")
        if (parser.expectOptional("AUTHORIZATION")) {
            val schema = PgSchema(
                ParserUtils.getObjectName(parser.parseIdentifier())
            )
            database.addSchema(schema)
            schema.authorization = schema.name
            val definition = parser.rest
            if (definition != null && !definition.isEmpty()) {
                schema.definition = definition
            }
        } else {
            val schema = PgSchema(
                ParserUtils.getObjectName(parser.parseIdentifier())
            )
            database.addSchema(schema)
            if (parser.expectOptional("AUTHORIZATION")) {
                schema.authorization = ParserUtils.getObjectName(parser.parseIdentifier())
            }
            val definition = parser.rest
            if (definition != null && !definition.isEmpty()) {
                schema.definition = definition
            }
        }
    }
}