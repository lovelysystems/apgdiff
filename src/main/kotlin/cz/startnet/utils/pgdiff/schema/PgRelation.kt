package cz.startnet.utils.pgdiff.schema

import cz.startnet.utils.pgdiff.PgDiffUtils

/**
 * base class for all relation types
 *
 * existing relations as of PG 12.x are, however not all are implemented
 * from https://www.postgresql.org/docs/12/catalog-pg-class.html
 * r = ordinary table,
 * i = index,
 * S = sequence,
 * t = TOAST table,
 * v = view,
 * m = materialized view,
 * c = composite type,
 * f = foreign table,
 * p = partitioned table,
 * I = partitioned index
 */
sealed class PgRelation<REL : PgRelation<REL, COL>, COL : PgColumnBase<REL, COL>>(name: String, objectType: String) :
    DBObject(objectType, name) {

    /**
     * List of columns defined on the relation.
     */
    val columns: MutableList<COL> = ArrayList()

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
     * Name of the index on which the table/matview is clustered
     */
    var clusterIndexName: String? = null

    /**
     * Tablespace value.
     */
    var tablespace: String? = null

    /**
     * List of privileges defined on the table.
     */
    val privileges: MutableList<PgRelationPrivilege> = ArrayList()

    /**
     * Finds column according to specified column `name`.
     *
     * @param name name of the column to be searched
     *
     * @return found column or null if no such column has been found
     */
    open fun getColumn(name: String): COL? {
        return columns.firstOrNull { it.name == name }
    }

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
        return indexes.firstOrNull { it.name == name }
    }

    /**
     * Finds trigger according to specified trigger `name`.
     *
     * @param name name of the trigger to be searched
     *
     * @return found trigger or null if no such trigger has been found
     */
    fun getTrigger(name: String): PgTrigger? {
        return triggers.firstOrNull { it.name == name }
    }


    /**
     * Adds `column` to the list of columns.
     *
     * @param column column
     */
    open fun addColumn(column: COL) {
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
    open val relationKind: String = objectType

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
        return getIndex(name) != null
    }

    fun addPrivilege(privilege: PgRelationPrivilege) {
        privileges.add(privilege)
    }

    fun getPrivilege(roleName: String?): PgRelationPrivilege? {
        return privileges.firstOrNull { it.roleName == roleName }
    }
}