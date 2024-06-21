/**
 * Copyright 2006 StartNet s.r.o.
 *
 * Distributed under MIT license
 */
package cz.startnet.utils.pgdiff.parsers

import cz.startnet.utils.pgdiff.schema.PgIndex

/**
 * Parses CREATE INDEX statements.
 *
 * @author fordfrog
 */
object CreateIndexParser : PatternBasedSubParser(
    "^CREATE[\\s]+(?:UNIQUE[\\s]+)?INDEX[\\s]+.*$"
) {
    /**
     * Parses CREATE INDEX statement.
     *
     * @param database  database
     * @param statement CREATE INDEX statement
     */
    override fun parse(parser: Parser, ctx: ParserContext) {
        parser.expect("CREATE")
        val unique = parser.expectOptional("UNIQUE")
        parser.expect("INDEX")
        parser.expectOptional("CONCURRENTLY")
        val indexName = ParserUtils.getObjectName(parser.parseIdentifier())
        parser.expect("ON")
        val index = PgIndex(indexName)
        if (parser.expectOptional("ONLY")) {
            index.only = true
        }
        val tableName = parser.parseIdentifier()
        val definition = parser.rest
        val schemaName = ParserUtils.getSchemaName(tableName, ctx.database)
        val schema = parser.withErrorContext {
            ctx.database.getSchemaSafe(schemaName)
        }
        val objectName = ParserUtils.getObjectName(tableName)
        val rel = schema.getView(objectName) ?: schema.getTableSafe(objectName)
        rel.addIndex(index)
        schema.addIndex(index)
        index.definition = definition!!.trim { it <= ' ' }
        index.tableName = objectName
        index.isUnique = unique
    }
}