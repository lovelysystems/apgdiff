package cz.startnet.utils.pgdiff.schema

import cz.startnet.utils.pgdiff.PgDiffUtils
import java.util.*

class PgType(val name: String) {

    var owner: String? = null

    /**
     * List of columns defined on the type
     */
    val columns: MutableList<PgColumn> = ArrayList()
    private val enumValues: MutableList<String?> = ArrayList()
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
            if (owner != null) {
                sbSQL.append(System.getProperty("line.separator"))
                sbSQL.append(System.getProperty("line.separator"))
                sbSQL.append(ownerSQL)
            }
            return sbSQL.toString()
        }

    val ownerSQL: String
        get() = "ALTER TYPE ${PgDiffUtils.getQuotedName(name)} OWNER TO $owner;"

    /**
     * Creates and returns SQL statement for dropping the table.
     *
     * @return created SQL statement
     */
    val dropSQL: String
        get() = "DROP TYPE " + PgDiffUtils.dropIfExists + PgDiffUtils.getQuotedName(name) + ";"

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
        get() {
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