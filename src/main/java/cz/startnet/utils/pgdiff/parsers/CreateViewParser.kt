/**
 * Copyright 2006 StartNet s.r.o.
 *
 * Distributed under MIT license
 */
package cz.startnet.utils.pgdiff.parsers

import cz.startnet.utils.pgdiff.Resources
import cz.startnet.utils.pgdiff.schema.PgDatabase
import cz.startnet.utils.pgdiff.schema.PgView
import java.text.MessageFormat
import java.util.*

/**
 * Parses CREATE VIEW statements.
 *
 * @author fordfrog
 */
object CreateViewParser {
    /**
     * Parses CREATE VIEW statement.
     *
     * @param database  database
     * @param statement CREATE VIEW statement
     */
    fun parse(
        database: PgDatabase,
        statement: String
    ) {
        val parser = Parser(statement)
        parser.expect("CREATE")
        parser.expectOptional("OR", "REPLACE")
        val materialized = parser.expectOptional("MATERIALIZED")
        val temporary = parser.expectOptional("TEMPORARY")
        val recursive = parser.expectOptional("RECURSIVE")
        parser.expect("VIEW")
        val viewName = parser.parseIdentifier()
        val with = StringBuilder()
        if (parser.expectOptional("WITH")) {
            parser.expect("(")
            while (!parser.expectOptional(")")) {
                with.append(parser.expression)
            }
        }
        val columnsExist = parser.expectOptional("(")
        val columnNames: MutableList<String> = ArrayList(10)
        if (columnsExist) {
            while (!parser.expectOptional(")")) {
                columnNames.add(
                    ParserUtils.getObjectName(parser.parseIdentifier())
                )
                parser.expectOptional(",")
            }
        }
        parser.expect("AS")
        val query = parser.rest!!
        val view = PgView(ParserUtils.getObjectName(viewName))
        view.isMaterialized = materialized
        view.isTemporary = temporary
        view.isRecursive = recursive
        view.with = with.toString()
        view.declaredColumnNames = columnNames
        view.query = query
        val schemaName = ParserUtils.getSchemaName(viewName, database)
        val schema = database.getSchema(schemaName)
            ?: throw RuntimeException(
                MessageFormat.format(
                    Resources.getString("CannotFindSchema"), schemaName,
                    statement
                )
            )
        schema.addRelation(view)
    }
}