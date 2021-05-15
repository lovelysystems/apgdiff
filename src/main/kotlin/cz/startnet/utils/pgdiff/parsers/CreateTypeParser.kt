package cz.startnet.utils.pgdiff.parsers

import cz.startnet.utils.pgdiff.schema.PgType
import cz.startnet.utils.pgdiff.schema.PgTypeColumn

/**
 * Parses CREATE TABLE statements.
 *
 * @author fordfrog
 */
object CreateTypeParser : PatternBasedSubParser(
    "^CREATE[\\s]+TYPE[\\s]+.*$"
) {
    override fun parse(parser: Parser, ctx: ParserContext) {
        parser.expect("CREATE", "TYPE")
        val objectName = ctx.database.getSchemaObjectName(parser.parseIdentifier())
        val schema = ctx.database.getSchema(objectName)
        val type = PgType(objectName.name)
        schema.addType(type)
        parser.expect("AS")
        if (parser.expectOptional("ENUM")) {
            type.isEnum = true
        }
        parser.expect("(")
        while (!parser.expectOptional(")")) {
            if (type.isEnum) {
                val name = parser.expression
                type.addEnumValue(name)
                if (parser.expectOptional(")")) {
                    break
                } else {
                    parser.expect(",")
                }
            } else {
                parseColumn(parser, type)
                if (parser.expectOptional(")")) {
                    break
                } else {
                    parser.expect(",")
                }
            }
        }
        while (!parser.expectOptional(";")) {
        }
    }

    /**
     * Parses column definition.
     *
     * @param parser parser
     * @param type type
     */
    private fun parseColumn(parser: Parser, type: PgType) {
        val column = PgTypeColumn(
            type,
            ParserUtils.getObjectName(parser.parseIdentifier())
        )
        type.addColumn(column)
        column.parseDefinition(parser.expression)
    }
}