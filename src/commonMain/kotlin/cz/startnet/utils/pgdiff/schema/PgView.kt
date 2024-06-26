/**
 * Copyright 2006 StartNet s.r.o.
 *
 * Distributed under MIT license
 */
package cz.startnet.utils.pgdiff.schema

import cz.startnet.utils.pgdiff.PgDiffUtils

/**
 * Stores view information.
 *
 * @author fordfrog
 */
sealed class PgViewBase(name: String, objectType: String, position: Int) : PgRelation<PgViewBase, PGViewColumn>(
    name, objectType,
    position
) {
    /**
     * Were column names explicitly declared as part of the view?
     */
    private var declareColumnNames = false
    /**
     * Getter for [.query].
     *
     * @return [.query]
     */
    /**
     * Setter for [.query].
     *
     * @param query [.query]
     */
    /**
     * SQL query of the view.
     */
    lateinit var query: String
    /**
     * Getter for [.with].
     *
     * @return [.with]
     */
    /**
     * Setter for [.with].
     *
     * @param with [.with]
     */
    /**
     * Is this a view with security_barrier?
     */
    var with: String? = null
    /**
     * Getter for [.temporary].
     *
     * @return [.temporary]
     */
    /**
     * Setter for [.temporary].
     *
     * @param temporary [.temporary]
     */
    /**
     * Is this a TEMPORARY view?
     */
    var isTemporary = false
    /**
     * Getter for [.recursive].
     *
     * @return [.recursive]
     */
    /**
     * Setter for [.recursive].
     *
     * @param recursive [.recursive]
     */
    /**
     * Is this a RECURSIVE view?
     */
    var isRecursive = false
    /**
     * Returns a list of column names if the names were declared along with the view, null otherwise.
     *
     * @return list of column names or null
     */// Can only be set once for a view, before defaults/comments are set
    /**
     * Sets the list of declared column names for the view.
     *
     * @param columnNames list of column names
     */
    var declaredColumnNames: List<String>?
        get() {
            val list: MutableList<String> = ArrayList()
            if (!declareColumnNames) return null
            for (column in columns) {
                list.add(column.name)
            }
            return list
        }
        set(columnNames) {
            // Can only be set once for a view, before defaults/comments are set
            require(!declareColumnNames)
            require(columns.isEmpty())
            if (columnNames.isNullOrEmpty()) return
            declareColumnNames = true
            for (colName in columnNames) {
                addColumn(PGViewColumn(this, colName))
            }
        }

    /**
     * Creates and returns SQL for creation of the view.
     *
     * @return created SQL statement
     */
    val creationSQL: String
        get() {
            val sbSQL = StringBuilder(query.length * 2)
            sbSQL.append("CREATE ")
            sbSQL.append(relationKind)
            sbSQL.append(' ')
            sbSQL.append(PgDiffUtils.getQuotedName(name))
            if (declareColumnNames) {
                require(columns.isNotEmpty())
                sbSQL.append(" (")
                for (i in columns.indices) {
                    if (i > 0) {
                        sbSQL.append(", ")
                    }
                    sbSQL.append(PgDiffUtils.getQuotedName(columns[i].name))
                }
                sbSQL.append(')')
            }
            if (!with.isNullOrEmpty()) {
                sbSQL.append(" WITH (")
                sbSQL.append(with)
                sbSQL.append(")")
            }
            sbSQL.append(" AS")
            sbSQL.appendLine()
            sbSQL.append("\t")
            sbSQL.append(query)
            sbSQL.append(';')

            /* Column default values */for (col in columns) {
                val defaultValue = col.defaultValue
                if (!defaultValue.isNullOrEmpty()) {
                    sbSQL.appendLine()
                    sbSQL.appendLine()
                    sbSQL.append("ALTER ")
                    sbSQL.append(relationKind)
                    sbSQL.append(' ')
                    sbSQL.append(PgDiffUtils.getQuotedName(name))
                    sbSQL.append(" ALTER COLUMN ")
                    sbSQL.append(PgDiffUtils.getQuotedName(col.name))
                    sbSQL.append(" SET DEFAULT ")
                    sbSQL.append(defaultValue)
                    sbSQL.append(';')
                }
            }
            sbSQL.append(commentDefinitionSQL)
            return sbSQL.toString()
        }

    /**
     * Finds column according to specified column `name`.
     *
     * @param name name of the column to be searched
     *
     * @return found column or null if no such column has been found
     */
    override fun getColumn(name: String): PGViewColumn? {
        var col = super.getColumn(name)
        if (col == null && !declareColumnNames) {
            /*
             * In views, we don't always know columns beforehand; create a new
             * column if the view didn't declare col names.
             */
            col = PGViewColumn(this, name)
            addColumn(col)
        }
        return col
    }

    /**
     * Returns true if table contains given column `name`, otherwise
     * false.
     *
     * @param name name of the column
     *
     * @return true if table contains given column `name`, otherwise false
     */
    override fun containsColumn(name: String?): Boolean {
        return true
    }
}


class PgView(name: String, position: Int) : PgViewBase(name, "VIEW", position)
class PgMaterializedView(name: String, position: Int) : PgViewBase(name, "MATERIALIZED VIEW", position)
