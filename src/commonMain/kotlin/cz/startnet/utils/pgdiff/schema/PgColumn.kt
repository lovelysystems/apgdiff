package cz.startnet.utils.pgdiff.schema

import cz.startnet.utils.pgdiff.PgDiffUtils

// GENERATED { ALWAYS | BY DEFAULT } AS IDENTITY [ ( sequence_options ) ]
sealed class GeneratedDef {
    abstract fun sql(writer: StringBuilder)
}

data class IdentityColumnDef(
    val always: Boolean,
    val sequenceOptions: String? = null
) : GeneratedDef() {
    override fun sql(writer: StringBuilder) {
        writer.append(" ADD GENERATED")
        if (always) {
            writer.append(" ALWAYS")
        } else {
            writer.append(" BY DEFAULT")
        }
        writer.append(" AS IDENTITY")
        sequenceOptions?.let {
            writer.append(" (\n    $sequenceOptions\n)")
        }
    }
}

sealed class PgColumnBase<REL : PgRelation<REL, COL>, COL : PgColumnBase<REL, COL>>(
    val relation: REL,
    val name: String,
    var comment: String? = null,
    var nullValue: Boolean = true,
    var defaultValue: String? = null
) {
    var statistics: Int? = null

    /**
     * Type of the column. Always null for view columns.
     */
    var type: String? = null

    /**
     * Contains information about column storage type.
     */
    var storage: String? = null

    var generated: GeneratedDef? = null

    /**
     * List of privileges defined on the table.
     */
    val privileges: MutableList<PgColumnPrivilege> = ArrayList()

    /**
     * Returns full definition of the column.
     *
     * @param addDefaults whether default value should be added in case NOT NULL
     * constraint is specified but no default value is set
     *
     * @return full definition of the column
     */
    fun getFullDefinition(addDefaults: Boolean): String {
        val sbDefinition = StringBuilder(100)
        sbDefinition.append(quotedIdentifier())
        sbDefinition.append(' ')
        sbDefinition.append(type)
        if (defaultValue != null && defaultValue!!.isNotEmpty()) {
            sbDefinition.append(" DEFAULT ")
            sbDefinition.append(defaultValue)
        } else if (!nullValue && addDefaults) {
            val defaultColValue = PgColumnUtils.getDefaultValue(type)
            if (defaultColValue != null) {
                sbDefinition.append(" DEFAULT ")
                sbDefinition.append(defaultColValue)
            }
        }
        if (!nullValue) {
            sbDefinition.append(" NOT NULL")
        }
        return sbDefinition.toString()
    }

    fun addPrivilege(privilege: PgColumnPrivilege) {
        privileges.add(privilege)
    }

    fun getPrivilege(roleName: String?): PgColumnPrivilege? {
        for (privilege in privileges) {
            if (privilege.roleName == roleName) {
                return privilege
            }
        }
        return null
    }

    private fun quotedIdentifier() = PgDiffUtils.getQuotedName(name)

    fun commentSQL(writer: StringBuilder) {
        val commentStr = comment ?: "NULL"
        writer.appendLine(
            "COMMENT ON COLUMN ${relation.quotedIdentifier()}.${quotedIdentifier()} IS $commentStr;"
        )
    }


    /**
     * Parses definition of the column
     *
     * @param definition definition of the column
     */
    fun parseDefinition(definition: String?) {
        requireNotNull(definition) { "Definition must not be null" }
        var string = definition
        var matcher = PATTERN_NOT_NULL.matchEntire(string)
        if (matcher!=null) {
            string = matcher.groupValues[1].trim { it <= ' ' }
            nullValue = false
        } else {
            matcher = PATTERN_NULL.matchEntire(string)
            if (matcher!=null) {
                string = matcher.groupValues[1].trim { it <= ' ' }
                nullValue = true
            }
        }
        matcher = PATTERN_DEFAULT.matchEntire(string)
        if (matcher!=null) {
            string = matcher.groupValues[1].trim { it <= ' ' }
            defaultValue = matcher.groupValues[2].trim { it <= ' ' }
        }
        type = string
    }

}

class PgInheritedColumn(table: PgTableBase, val inheritedColumn: PgColumn) :
    PgColumnBase<PgTableBase, PgColumn>(table, inheritedColumn.name)

class PGViewColumn(view: PgViewBase, name: String) : PgColumnBase<PgViewBase, PGViewColumn>(view, name)

class PgColumn(table: PgTableBase, name: String) : PgColumnBase<PgTableBase, PgColumn>(table, name)

class PgTypeColumn(type: PgType, name: String) : PgColumnBase<PgType, PgTypeColumn>(type, name)

/**
 * Pattern for parsing NULL arguments.
 */
private val PATTERN_NULL = Regex("^(.+)[\\s]+NULL$", RegexOption.IGNORE_CASE)

/**
 * Pattern for parsing NOT NULL arguments.
 */
private val PATTERN_NOT_NULL = Regex("^(.+)[\\s]+NOT[\\s]+NULL$", RegexOption.IGNORE_CASE)

/**
 * Pattern for parsing DEFAULT value.
 */
private val PATTERN_DEFAULT = Regex("^(.+)[\\s]+DEFAULT[\\s]+(.+)$", RegexOption.IGNORE_CASE)
