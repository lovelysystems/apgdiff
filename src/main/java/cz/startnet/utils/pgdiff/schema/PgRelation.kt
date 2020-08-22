/**
 * Copyright 2006 StartNet s.r.o.
 *
 * Distributed under MIT license
 */
package cz.startnet.utils.pgdiff.schema

import cz.startnet.utils.pgdiff.PgDiffUtils
import java.util.*

/**
 * Base class for tables and views.
 *
 * @author Marti Raudsepp
 */
abstract class PgRelation {
    /**
     * List of columns defined on the relation.
     */
    val columns: MutableList<PgColumn> = ArrayList()

    /**
     * List of indexes defined on the relation.
     */
    val indexes: MutableList<PgIndex> = ArrayList()

    /**
     * List of triggers defined on the table/view.
     */
    val triggers: MutableList<PgTrigger> = ArrayList()

    /**
     * List of rules defined on the table/view.
     */
    val rules: MutableList<PgRule> = ArrayList()
    /**
     * Getter for [.clusterIndexName].
     *
     * @return [.clusterIndexName]
     */
    /**
     * Setter for [.clusterIndexName].
     *
     * @param name [.clusterIndexName]
     */
    /**
     * Name of the index on which the table/matview is clustered
     */
    var clusterIndexName: String? = null
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
     * Name of the relation.
     */
    var name: String? = null
    /**
     * Getter for [.tablespace].
     *
     * @return [.tablespace]
     */
    /**
     * Setter for [.tablespace].
     *
     * @param tablespace [.tablespace]
     */
    /**
     * Tablespace value.
     */
    open var tablespace: String? = null
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
    val privileges: MutableList<PgRelationPrivilege> = ArrayList()
    /**
     * Getter for [.ownerTo].
     *
     * @return [.ownerTo]
     */
    /**
     * Setter for [.ownerTo].
     *
     * @param ownerTo
     * [.ownerTo]
     */
    /**
     * Column the table is owner to.
     */
    var ownerTo: String? = null

    /**
     * Finds column according to specified column `name`.
     *
     * @param name name of the column to be searched
     *
     * @return found column or null if no such column has been found
     */
    open fun getColumn(name: String): PgColumn? {
        for (column in columns) {
            if (column.name == name) {
                return column
            }
        }
        return null
    }

//    /**
//     * Getter for [.columns]. The list cannot be modified.
//     *
//     * @return [.columns]
//     */
//    fun getColumns(): List<PgColumn> {
//        return Collections.unmodifiableList(columns)
//    }

    /**
     * Generates SQL code for declaring relation and column comments
     *
     * @return SQL code for declaring relation and column comments
     */
    protected val commentDefinitionSQL: String
        protected get() {
            val sbSQL = StringBuilder(100)
            if (comment != null && !comment!!.isEmpty()) {
                sbSQL.append(System.getProperty("line.separator"))
                sbSQL.append(System.getProperty("line.separator"))
                sbSQL.append("COMMENT ON ")
                sbSQL.append(relationKind)
                sbSQL.append(' ')
                sbSQL.append(PgDiffUtils.getQuotedName(name))
                sbSQL.append(" IS ")
                sbSQL.append(comment)
                sbSQL.append(';')
            }
            for (column in columns) {
                if (!column.comment.isNullOrEmpty()) {
                    sbSQL.append(System.getProperty("line.separator"))
                    sbSQL.append(System.getProperty("line.separator"))
                    sbSQL.append("COMMENT ON COLUMN ")
                    sbSQL.append(PgDiffUtils.getQuotedName(name))
                    sbSQL.append('.')
                    sbSQL.append(PgDiffUtils.getQuotedName(column.name))
                    sbSQL.append(" IS ")
                    sbSQL.append(column.comment)
                    sbSQL.append(';')
                }
            }
            return sbSQL.toString()
        }

    /**
     * Finds index according to specified index `name`.
     *
     * @param name name of the index to be searched
     *
     * @return found index or null if no such index has been found
     */
    fun getIndex(name: String): PgIndex? {
        for (index in indexes) {
            if (index.name == name) {
                return index
            }
        }
        return null
    }

    /**
     * Finds trigger according to specified trigger `name`.
     *
     * @param name name of the trigger to be searched
     *
     * @return found trigger or null if no such trigger has been found
     */
    fun getTrigger(name: String?): PgTrigger? {
        for (trigger in triggers) {
            if (trigger.name == name) {
                return trigger
            }
        }
        return null
    }

//    /**
//     * Getter for [.indexes]. The list cannot be modified.
//     *
//     * @return [.indexes]
//     */
//    fun getIndexes(): List<PgIndex> {
//        return Collections.unmodifiableList(indexes)
//    }

//    /**
//     * Getter for [.triggers]. The list cannot be modified.
//     *
//     * @return [.triggers]
//     */
//    fun getTriggers(): List<PgTrigger> {
//        return Collections.unmodifiableList(triggers)
//    }

//    /**
//     * Getter for [.rules]. The list cannot be modified.
//     *
//     * @return [.rules]
//     */
//    fun getRules(): List<PgRule> {
//        return Collections.unmodifiableList(rules)
//    }

    /**
     * Adds `column` to the list of columns.
     *
     * @param column column
     */
    open fun addColumn(column: PgColumn) {
        columns.add(column)
    }

    /**
     * Adds `index` to the list of indexes.
     *
     * @param index index
     */
    fun addIndex(index: PgIndex) {
        indexes.add(index)
    }

    /**
     * Adds `trigger` to the list of triggers.
     *
     * @param trigger trigger
     */
    fun addTrigger(trigger: PgTrigger) {
        triggers.add(trigger)
    }

    /**
     * Adds `rule` to the list of rules.
     *
     * @param rule rule
     */
    fun addRule(rule: PgRule) {
        rules.add(rule)
    }

    /**
     * Returns relation kind for CREATE/ALTER/DROP commands.
     *
     * @return relation kind
     */
    abstract val relationKind: String

    /**
     * Creates and returns SQL statement for dropping the relation.
     *
     * @return created SQL statement
     */
    open val dropSQL: String
        get() = "DROP " + relationKind + " " + PgDiffUtils.dropIfExists +
                PgDiffUtils.getQuotedName(name) + ";"

    /**
     * Returns true if table contains given column `name`, otherwise
     * false.
     *
     * @param name name of the column
     *
     * @return true if table contains given column `name`, otherwise false
     */
    open fun containsColumn(name: String?): Boolean {
        for (column in columns) {
            if (column.name == name) {
                return true
            }
        }
        return false
    }

    /**
     * Finds inheritedColumn according to specified name `name`.
     *
     * @param name name of the inheritedColumn to be searched
     *
     * @return found inheritedColumn or null if no such inheritedColumn
     * has been found
     */
    open fun getInheritedColumn(name: String?): PgInheritedColumn? {
        return null
    }

    /**
     * Returns true if table contains given inheritedColumn `name`,
     * otherwise false.
     *
     * @param name name of the inheritedColumn
     *
     * @return true if table contains given inheritedColumn `name`,
     * otherwise false
     */
    open fun containsInheritedColumn(name: String?): Boolean {
        return false
    }

    /**
     * Returns true if table/matview contains given index `name`, otherwise false.
     *
     * @param name name of the index
     *
     * @return true if table/matview contains given index `name`, otherwise false
     */
    fun containsIndex(name: String): Boolean {
        for (index in indexes) {
            if (index.name == name) {
                return true
            }
        }
        return false
    }

//    fun getPrivileges(): List<PgRelationPrivilege> {
//        return Collections.unmodifiableList(privileges)
//    }

    fun addPrivilege(privilege: PgRelationPrivilege) {
        privileges.add(privilege)
    }

    fun getPrivilege(roleName: String?): PgRelationPrivilege? {
        for (privilege in privileges) {
            if (privilege.roleName == roleName) {
                return privilege
            }
        }
        return null
    }
}