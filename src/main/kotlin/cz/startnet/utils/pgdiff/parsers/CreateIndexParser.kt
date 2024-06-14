/**
 * Copyright 2006 StartNet s.r.o.
 *
 * Distributed under MIT license
 */
package cz.startnet.utils.pgdiff.parsers

import cz.startnet.utils.pgdiff.Resources
import cz.startnet.utils.pgdiff.schema.PgIndex
import java.text.MessageFormat

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
        val table = schema.getTable(objectName)
        val view = schema.getView(objectName)
        if (table != null) {
            table.addIndex(index)
        } else if (view != null) {
            view.addIndex(index)
        } else {
            throw RuntimeException(
                MessageFormat.format(
                    Resources.getString("CannotFindObject"), tableName,
                    parser.string
                )
            )
        }
        schema.addIndex(index)
        index.definition = definition!!.trim { it <= ' ' }
        index.tableName = objectName
        index.isUnique = unique
    }
}