/**
 * Copyright 2006 StartNet s.r.o.
 *
 * Distributed under MIT license
 */
package cz.startnet.utils.pgdiff.schema

import cz.startnet.utils.pgdiff.PgDiffUtils
import java.util.*

/**
 * Stores table information.
 *
 * @author fordfrog
 */
class PgType
/**
 * Creates a new PgTable object.
 *
 * @param name [.name]
 */(
    /**
     * Name of the table.
     */
    var name: String?
) {
    /**
     * List of columns defined on the table.
     */
    private val columns: MutableList<PgColumn> = ArrayList()
    private val enumValues: MutableList<String?> = ArrayList()
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
    var isEnum = false

    /**
     * Finds column according to specified column `name`.
     *
     * @param name name of the column to be searched
     *
     * @return found column or null if no such column has been found
     */
    fun getColumn(name: String?): PgColumn? {
        for (column in columns) {
            if (column.name == name) {
                return column
            }
        }
        return null
    }

    /**
     * Getter for [.columns]. The list cannot be modified.
     *
     * @return [.columns]
     */
    fun getColumns(): List<PgColumn> {
        return Collections.unmodifiableList(columns)
    }

    /**
     * Creates and returns SQL for creation of the table.
     *
     * @return created SQL statement
     */
    val creationSQL: String
        get() {
            val sbSQL = StringBuilder(1000)
            sbSQL.append("CREATE TYPE ")
            sbSQL.append(PgDiffUtils.getQuotedName(name))
            if (isEnum) {
                sbSQL.append(" AS ENUM (")
            } else {
                sbSQL.append(" AS (")
            }
            sbSQL.append(System.getProperty("line.separator"))
            var first = true
            if (isEnum) {
                for (enumValue in enumValues) {
                    if (first) {
                        first = false
                    } else {
                        sbSQL.append(",")
                        sbSQL.append(System.getProperty("line.separator"))
                    }
                    sbSQL.append("\t")
                    sbSQL.append(enumValue)
                }
                sbSQL.append(System.getProperty("line.separator"))
                sbSQL.append(")")
            } else {
                if (columns.isEmpty()) {
                    sbSQL.append(')')
                } else {
                    for (column in columns) {
                        if (first) {
                            first = false
                        } else {
                            sbSQL.append(",")
                            sbSQL.append(System.getProperty("line.separator"))
                        }
                        sbSQL.append("\t")
                        sbSQL.append(column.getFullDefinition(false))
                    }
                    sbSQL.append(System.getProperty("line.separator"))
                    sbSQL.append(")")
                }
            }
            sbSQL.append(';')
            for (column in columnsWithStatistics) {
                sbSQL.append(System.getProperty("line.separator"))
                sbSQL.append("ALTER TABLE ONLY ")
                sbSQL.append(PgDiffUtils.getQuotedName(name))
                sbSQL.append(" ALTER COLUMN ")
                sbSQL.append(
                    PgDiffUtils.getQuotedName(column.name)
                )
                sbSQL.append(';')
            }
            return sbSQL.toString()
        }

    /**
     * Creates and returns SQL statement for dropping the table.
     *
     * @return created SQL statement
     */
    val dropSQL: String
        get() = "DROP TYPE " + PgDiffUtils.getDropIfExists() + PgDiffUtils.getQuotedName(name) + ";"

    /**
     * Adds `column` to the list of columns.
     *
     * @param column column
     */
    fun addColumn(column: PgColumn) {
        columns.add(column)
    }

    /**
     * Returns true if table contains given column `name`, otherwise
     * false.
     *
     * @param name name of the column
     *
     * @return true if table contains given column `name`, otherwise false
     */
    fun containsColumn(name: String?): Boolean {
        for (column in columns) {
            if (column.name == name) {
                return true
            }
        }
        return false
    }

    /**
     * Returns list of columns that have statistics defined.
     *
     * @return list of columns that have statistics defined
     */
    private val columnsWithStatistics: List<PgColumn>
        private get() {
            val list: MutableList<PgColumn> = ArrayList()
            for (column in columns) {
                if (column.statistics != null) {
                    list.add(column)
                }
            }
            return list
        }

    fun addEnumValue(value: String?) {
        enumValues.add(value)
    }
}