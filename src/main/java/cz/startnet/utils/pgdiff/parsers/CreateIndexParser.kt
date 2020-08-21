/**
 * Copyright 2006 StartNet s.r.o.
 *
 * Distributed under MIT license
 */
package cz.startnet.utils.pgdiff.parsers

import cz.startnet.utils.pgdiff.Resources
import cz.startnet.utils.pgdiff.schema.PgDatabase
import cz.startnet.utils.pgdiff.schema.PgIndex
import java.text.MessageFormat

/**
 * Parses CREATE INDEX statements.
 *
 * @author fordfrog
 */
object CreateIndexParser {
    /**
     * Parses CREATE INDEX statement.
     *
     * @param database  database
     * @param statement CREATE INDEX statement
     */
    fun parse(
        database: PgDatabase,
        statement: String
    ) {
        val parser = Parser(statement)
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
        val schemaName = ParserUtils.getSchemaName(tableName, database)
        val schema = database.getSchema(schemaName)
                ?: throw RuntimeException(
                    MessageFormat.format(
                        Resources.getString("CannotFindSchema"), schemaName,
                        statement
                    )
                )
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
                    statement
                )
            )
        }
        schema.addIndex(index)
        index.definition = definition!!.trim { it <= ' ' }
        index.tableName = objectName
        index.isUnique = unique
    }
}