package cz.startnet.utils.pgdiff.parsers

import cz.startnet.utils.pgdiff.schema.IdentityColumnDef
import cz.startnet.utils.pgdiff.schema.PgColumnBase
import cz.startnet.utils.pgdiff.schema.PgDatabase
import cz.startnet.utils.pgdiff.schema.PgRelation

data class ParserContext(
    val database: PgDatabase,
)

/*
ALTER [ COLUMN ] column_name [ SET DATA ] TYPE data_type [ COLLATE collation ] [ USING expression ]
ALTER [ COLUMN ] column_name SET DEFAULT expression
ALTER [ COLUMN ] column_name DROP DEFAULT
ALTER [ COLUMN ] column_name { SET | DROP } NOT NULL
ALTER [ COLUMN ] column_name ADD GENERATED { ALWAYS | BY DEFAULT } AS IDENTITY [ ( sequence_options ) ]
ALTER [ COLUMN ] column_name { SET GENERATED { ALWAYS | BY DEFAULT } | SET sequence_option | RESTART [ [ WITH ] restart ] } [...]
ALTER [ COLUMN ] column_name DROP IDENTITY [ IF EXISTS ]
ALTER [ COLUMN ] column_name SET STATISTICS integer
ALTER [ COLUMN ] column_name SET ( attribute_option = value [, ... ] )
ALTER [ COLUMN ] column_name RESET ( attribute_option [, ... ] )
ALTER [ COLUMN ] column_name SET STORAGE { PLAIN | EXTERNAL | EXTENDED | MAIN }
*/
class AlterColumnParser(private val columnName: String, val parser: Parser, private val rel: PgRelation<*, *>, val ctx: ParserContext) {

    private fun getColumnSafe(): PgColumnBase<*, *> {
        return parser.withErrorContext { rel.getColumnSafe(columnName) }
    }

    private fun parseSet() = parser.withErrorContext {
        if (parser.expectOptional("STATISTICS")) {
            getColumnSafe().statistics = parser.parseInteger()
        } else if (parser.expectOptional("NOT NULL")) {
            val col = rel.getInheritedColumn(columnName) ?: rel.getColumnSafe(columnName)
            col.nullValue = false
        } else if (parser.expectOptional("DEFAULT")) {
            val defaultValue = parser.expression
            val col = rel.getInheritedColumn(columnName) ?: rel.getColumnSafe(columnName)
            col.defaultValue = defaultValue
        } else if (parser.expectOptional("STORAGE")) {
            val column = rel.getColumnSafe(columnName)
            if (parser.expectOptional("PLAIN")) {
                column.storage = "PLAIN"
            } else if (parser.expectOptional("EXTERNAL")) {
                column.storage = "EXTERNAL"
            } else if (parser.expectOptional("EXTENDED")) {
                column.storage = "EXTENDED"
            } else if (parser.expectOptional("MAIN")) {
                column.storage = "MAIN"
            } else {
                parser.throwUnsupportedCommand()
            }
        } else {
            parser.throwUnsupportedCommand()
        }
    }

    fun parse() {
        if (parser.expectOptional("SET")) {
            parseSet()
        } else if (parser.expectOptional("ADD", "GENERATED")) {
            // ADD GENERATED { ALWAYS | BY DEFAULT } AS IDENTITY [ ( sequence_options ) ]
            val col = getColumnSafe()
            val always = parser.expectOptional("ALWAYS")
            if (!always) {
                parser.expect("BY", "DEFAULT")
            }
            parser.expect("AS")
            if (parser.expectOptional("IDENTITY")) {
                if ((parser.expectOptional("("))) {
                    col.generated = IdentityColumnDef(always, parser.expression)
                    parser.expect(")")
                } else {
                    col.generated = IdentityColumnDef(always)
                }
            }
        } else {
            parser.throwUnsupportedCommand()
        }
    }


}
