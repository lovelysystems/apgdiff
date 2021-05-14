package cz.startnet.utils.pgdiff.schema

import cz.startnet.utils.pgdiff.PgDiffUtils

class PgType(name: String) : PgRelation<PgType, PgTypeColumn>(name, "TYPE") {

    /**
     * List of columns defined on the type
     */
    private val enumValues: MutableList<String?> = ArrayList()
    var isEnum = false

    /**
     * Finds column according to specified column `name`.
     *
     * @param name name of the column to be searched
     *
     * @return found column or null if no such column has been found
     */
    fun getColumn(name: String?): PgTypeColumn? {
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
            if (comment != null) {
                sbSQL.append(System.getProperty("line.separator"))
                sbSQL.append(System.getProperty("line.separator"))
                sbSQL.append(commentSQL)
            }
            return sbSQL.toString()
        }

    /**
     * Returns list of columns that have statistics defined.
     *
     * @return list of columns that have statistics defined
     */
    private val columnsWithStatistics: Collection<PgTypeColumn>
        get() {
            return columns.filter { it.statistics != null }
        }

    fun addEnumValue(value: String?) {
        enumValues.add(value)
    }
}