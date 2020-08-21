/**
 * Copyright 2006 StartNet s.r.o.
 *
 * Distributed under MIT license
 */
package cz.startnet.utils.pgdiff.parsers

import cz.startnet.utils.pgdiff.schema.PgDatabase
import cz.startnet.utils.pgdiff.schema.PgExtension
import cz.startnet.utils.pgdiff.schema.PgSchema

/**
 * Parses CREATE EXTENSION statements.
 *
 * @author atila
 */
object CreateExtensionParser {
    /**
     * Parses CREATE EXTENSION statement.
     *
     * @param database  database
     * @param statement CREATE EXTENSION statement
     */
    fun parse(
        database: PgDatabase,
        statement: String
    ) {
        val parser = Parser(statement)
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
        database.addExtension(extension)
    }
}