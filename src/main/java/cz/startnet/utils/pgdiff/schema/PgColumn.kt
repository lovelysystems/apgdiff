/**
 * Copyright 2006 StartNet s.r.o.
 *
 * Distributed under MIT license
 */
package cz.startnet.utils.pgdiff.schema

import cz.startnet.utils.pgdiff.PgDiffUtils
import java.util.*
import java.util.regex.Pattern

/**
 * Stores column information.
 *
 * @author fordfrog
 */
class PgColumn
/**
 * Creates a new PgColumn object.
 *
 * @param name name of the column
 */(
    /**
     * Name of the column.
     */
    var name: String?
) {
    /**
     * Getter for [.statistics].
     *
     * @return [.statistics]
     */
    /**
     * Setter for [.statistics].
     *
     * @param statistics [.statistics]
     */
    /**
     * Specific statistics value.
     */
    var statistics: Int? = null
    /**
     * Getter for [.defaultValue].
     *
     * @return [.defaultValue]
     */
    /**
     * Setter for [.defaultValue].
     *
     * @param defaultValue [.defaultValue]
     */
    /**
     * Default value of the column.
     */
    var defaultValue: String? = null
    /**
     * Getter for [.name].
     *
     * @return [.name]
     */
    /**
     * Setter for [.name].
     *
     * @param name [.name]
     */
    /**
     * Getter for [.type].
     *
     * @return [.type]
     */
    /**
     * Setter for [.type].
     *
     * @param type [.type]
     */
    /**
     * Type of the column. Always null for view columns.
     */
    var type: String? = null
    /**
     * Getter for [.nullValue].
     *
     * @return [.nullValue]
     */
    /**
     * Setter for [.nullValue].
     *
     * @param nullValue [.nullValue]
     */
    /**
     * Determines whether null value is allowed in the column.
     */
    var nullValue = true
    /**
     * Getter for [.storage].
     *
     * @return [.storage]
     */
    /**
     * Setter for [.storage].
     *
     * @param storage [.storage]
     */
    /**
     * Contains information about column storage type.
     */
    var storage: String? = null
    /**
     * Getter for [.comment].
     *
     * @return [.comment]
     */
    /**
     * Setter for [.comment].
     *
     * @param comment [.comment]
     */
    /**
     * Comment.
     */
    var comment: String? = null

    /**
     * List of privileges defined on the table.
     */
    private val privileges: MutableList<PgColumnPrivilege> = ArrayList()

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
        sbDefinition.append(PgDiffUtils.getQuotedName(name))
        sbDefinition.append(' ')
        sbDefinition.append(type)
        if (defaultValue != null && !defaultValue!!.isEmpty()) {
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

    fun getPrivileges(): List<PgColumnPrivilege> {
        return Collections.unmodifiableList(privileges)
    }

    /**
     * Parses definition of the column
     *
     * @param definition definition of the column
     */
    fun parseDefinition(definition: String?) {
        var string = definition
        var matcher = PATTERN_NOT_NULL.matcher(string)
        if (matcher.matches()) {
            string = matcher.group(1).trim { it <= ' ' }
            nullValue = false
        } else {
            matcher = PATTERN_NULL.matcher(string)
            if (matcher.matches()) {
                string = matcher.group(1).trim { it <= ' ' }
                nullValue = true
            }
        }
        matcher = PATTERN_DEFAULT.matcher(string)
        if (matcher.matches()) {
            string = matcher.group(1).trim { it <= ' ' }
            defaultValue = matcher.group(2).trim { it <= ' ' }
        }
        type = string
    }

    companion object {
        /**
         * Pattern for parsing NULL arguments.
         */
        private val PATTERN_NULL = Pattern.compile("^(.+)[\\s]+NULL$", Pattern.CASE_INSENSITIVE)

        /**
         * Pattern for parsing NOT NULL arguments.
         */
        private val PATTERN_NOT_NULL = Pattern.compile(
            "^(.+)[\\s]+NOT[\\s]+NULL$", Pattern.CASE_INSENSITIVE
        )

        /**
         * Pattern for parsing DEFAULT value.
         */
        private val PATTERN_DEFAULT = Pattern.compile(
            "^(.+)[\\s]+DEFAULT[\\s]+(.+)$", Pattern.CASE_INSENSITIVE
        )
    }
}