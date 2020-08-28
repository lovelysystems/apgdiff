package cz.startnet.utils.pgdiff.parsers

import cz.startnet.utils.pgdiff.Resources
import cz.startnet.utils.pgdiff.schema.IdentityColumnDef
import cz.startnet.utils.pgdiff.schema.PgColumn
import cz.startnet.utils.pgdiff.schema.PgDatabase
import cz.startnet.utils.pgdiff.schema.PgRelation
import java.text.MessageFormat

data class ParserContext(
    val database: PgDatabase,
    val outputIgnoredStatements: Boolean = false
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
class AlterColumnParser(val columnName: String, val parser: Parser, val rel: PgRelation, val ctx: ParserContext) {

    fun getColumnSafe(): PgColumn {
        return rel.getColumn(columnName)
            ?: throw RuntimeException(
                MessageFormat.format(
                    Resources.getString("CannotFindTableColumn"),
                    columnName, rel.name, parser.string
                )
            )

    }

    fun parseSet() {
        if (parser.expectOptional("STATISTICS")) {
            getColumnSafe().statistics = parser.parseInteger()
        } else if (parser.expectOptional("NOT NULL")) {
            if (rel.containsColumn(columnName)) {
                getColumnSafe().nullValue = false
            } else if (rel.containsInheritedColumn(columnName)) {
                val inheritedColumn = rel.getInheritedColumn(columnName)
                    ?: throw RuntimeException(
                        MessageFormat.format(
                            Resources.getString("CannotFindTableColumn"),
                            columnName, rel.name, parser.string
                        )
                    )
                inheritedColumn.nullValue = false
            } else {
                throw ParserException(
                    MessageFormat.format(
                        Resources.getString("CannotFindColumnInTable"),
                        columnName, rel.name
                    )
                )
            }
        } else if (parser.expectOptional("DEFAULT")) {
            val defaultValue = parser.expression
            if (rel.containsColumn(columnName)) {
                val column = rel.getColumn(columnName)
                    ?: throw RuntimeException(
                        MessageFormat.format(
                            Resources.getString("CannotFindTableColumn"),
                            columnName, rel.name,
                            parser.string
                        )
                    )
                column.defaultValue = defaultValue
            } else if (rel.containsInheritedColumn(columnName)) {
                val column = rel.getInheritedColumn(columnName)
                    ?: throw RuntimeException(
                        MessageFormat.format(
                            Resources.getString("CannotFindTableColumn"),
                            columnName, rel.name,
                            parser.string
                        )
                    )
                column.defaultValue = defaultValue
            } else {
                throw ParserException(
                    MessageFormat.format(
                        Resources.getString("CannotFindColumnInTable"),
                        columnName, rel.name
                    )
                )
            }
        } else if (parser.expectOptional("STORAGE")) {
            val column = rel.getColumn(columnName)
                ?: throw RuntimeException(
                    MessageFormat.format(
                        Resources.getString("CannotFindTableColumn"),
                        columnName, rel.name, parser.string
                    )
                )
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