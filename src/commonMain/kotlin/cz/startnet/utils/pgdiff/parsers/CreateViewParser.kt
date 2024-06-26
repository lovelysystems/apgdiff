package cz.startnet.utils.pgdiff.parsers

import cz.startnet.utils.pgdiff.schema.PgMaterializedView
import cz.startnet.utils.pgdiff.schema.PgView

/**
 * Parses CREATE VIEW statements.
 *
 * @author fordfrog
 */
object CreateViewParser : PatternBasedSubParser(
    "^CREATE[\\s]+(?:OR[\\s]+REPLACE[\\s]+)?(?:MATERIALIZED[\\s]+)?VIEW[\\s]+.*$"
) {
    override fun parse(parser: Parser, ctx: ParserContext) {
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

        val view = if (materialized) {
            PgMaterializedView(ParserUtils.getObjectName(viewName), parser.statementNum)
        } else {
            PgView(ParserUtils.getObjectName(viewName), parser.statementNum)
        }

        view.isTemporary = temporary
        view.isRecursive = recursive
        view.with = with.toString()
        view.declaredColumnNames = columnNames
        view.query = query
        val schemaName = ParserUtils.getSchemaName(viewName, ctx.database)
        val schema = parser.withErrorContext {
            ctx.database.getSchemaSafe(schemaName)
        }
        schema.addRelation(view)
    }
}