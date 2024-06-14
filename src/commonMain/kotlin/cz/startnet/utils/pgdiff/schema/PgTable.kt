package cz.startnet.utils.pgdiff.schema

import cz.startnet.utils.pgdiff.PgDiffUtils

sealed class PgTableBase(
    name: String,
    objectType: String,
    private val database: PgDatabase,
    private val schema: PgSchema, position: Int
) : PgRelation<PgTableBase, PgColumn>(name, objectType, position) {
    /**
     * List of inheritedColumns defined on the table.
     */
    val inheritedColumns: MutableList<PgInheritedColumn> = ArrayList()

    /**
     * List of constraints defined on the table.
     */
    val constraints: MutableList<PgConstraint> = ArrayList()

    /**
     * List of names of inherited tables.
     */
    val inherits: MutableList<Pair<String, String>> = ArrayList()

    /**
     * The with clause, currently not implemented
     */
    var with: String? = null

    /**
     * Is this a UNLOGGED table?
     */
    var isUnlogged = false

    /**
     * Does this table have RLS enabled?
     */
    private var rlsEnabled: Boolean? = null

    /**
     * Does this table have RLS forced?
     */
    private var rlsForced: Boolean? = null
    var foreignServer: String? = null
    var rangePartition: String? = null

    /**
     * RLS Policies
     */
    val policies: MutableList<PgPolicy> = ArrayList()

    /**
     * Finds constraint according to specified constraint `name`.
     *
     * @param name name of the constraint to be searched
     *
     * @return found constraint or null if no such constraint has been found
     */
    fun getConstraint(name: String?): PgConstraint? {
        for (constraint in constraints) {
            if (constraint.name == name) {
                return constraint
            }
        }
        return null
    }

    /**
     * Returns relation kind for CREATE/ALTER/DROP commands.
     *
     * @return relation kind
     */
    override val relationKind: String
        get() = "TABLE"

    /**
     * Creates and returns SQL for creation of the table.
     *
     * @param schema schema of current statement
     *
     * @return created SQL statement
     */
    fun getCreationSQL(schema: PgSchema): String {
        val sbSQL = StringBuilder(1000)
        sbSQL.append("CREATE ")
        if (isUnlogged) {
            sbSQL.append("UNLOGGED ")
        }
        sbSQL.append("$objectType ")
        sbSQL.append(PgDiffUtils.createIfNotExists)
        sbSQL.append(PgDiffUtils.getQuotedName(name))
        sbSQL.append(" (")
        sbSQL.append(System.getProperty("line.separator"))
        var first = true
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
        if (inherits.isNotEmpty()) {
            sbSQL.append(System.getProperty("line.separator"))
            sbSQL.append("INHERITS (")
            first = true
            for (inheritPair in inherits) {
                if (first) {
                    first = false
                } else {
                    sbSQL.append(", ")
                }
                var inheritTableName: String?
                inheritTableName = if (schema.name == inheritPair.first) {
                    inheritPair.second
                } else {
                    String.format("%s.%s", inheritPair.first, inheritPair.second)
                }
                sbSQL.append(inheritTableName)
            }
            sbSQL.append(")")
        }
        if (with != null && !with!!.isEmpty()) {
            TODO("with clause in table creation not supported")
        }
        if (this is PgForeignTable) {
            sbSQL.append("SERVER ")
            sbSQL.append(foreignServer)
        }
        if (tablespace != null && !tablespace!!.isEmpty()) {
            sbSQL.append(System.getProperty("line.separator"))
            sbSQL.append("TABLESPACE ")
            sbSQL.append(tablespace)
        }
        if (rangePartition != null && !rangePartition!!.isEmpty()) {
            sbSQL.append(System.getProperty("line.separator"))
            sbSQL.append("PARTITION BY RANGE ")
            sbSQL.append(rangePartition)
        }
        sbSQL.append(';')

        //Inherited column default override
        for (column in inheritedColumns) {
            if (column.defaultValue != null) {
                sbSQL.append(System.getProperty("line.separator"))
                sbSQL.append(System.getProperty("line.separator"))
                sbSQL.append("ALTER TABLE ONLY ")
                sbSQL.append(PgDiffUtils.getQuotedName(name))
                sbSQL.append(System.getProperty("line.separator"))
                sbSQL.append("\tALTER COLUMN ")
                sbSQL.append(
                    PgDiffUtils.getQuotedName(column.inheritedColumn.name)
                )
                sbSQL.append(" SET DEFAULT ")
                sbSQL.append(column.defaultValue)
                sbSQL.append(';')
            }
        }
        for (column in columnsWithStatistics) {
            sbSQL.append(System.getProperty("line.separator"))
            sbSQL.append("ALTER TABLE ONLY ")
            sbSQL.append(PgDiffUtils.getQuotedName(name))
            sbSQL.append(" ALTER COLUMN ")
            sbSQL.append(
                PgDiffUtils.getQuotedName(column.name)
            )
            sbSQL.append(" SET STATISTICS ")
            sbSQL.append(column.statistics)
            sbSQL.append(';')
        }
        sbSQL.append(commentDefinitionSQL)
        return sbSQL.toString()
    }

    /**
     * Setter for [.inherits].
     *
     * @param schemaName name of schema
     * @param tableName name of inherited table
     */
    fun addInherits(schemaName: String, tableName: String) {
        inherits.add(Pair(schemaName, tableName))
        val inheritedTable = database.getSchema(schemaName)!!.getTable(tableName)
        for (column in inheritedTable!!.columns) {
            val inheritedColumn = PgInheritedColumn(this, column)
            inheritedColumns.add(inheritedColumn)
        }
        for (column in inheritedTable.inheritedColumns) {
            val inheritedColumn = PgInheritedColumn(this, column.inheritedColumn)
            inheritedColumns.add(inheritedColumn)
        }
    }

    /**
     * Adds `column` to the list of columns.
     *
     * @param column column
     */
    override fun addColumn(column: PgColumn) {
        columns.add(column)
    }

    /**
     * Adds `inheritedColumn` to the list of inheritedColumns.
     *
     * @param inheritedColumn inheritedColumn
     */
    fun addInheritedColumn(inheritedColumn: PgInheritedColumn) {
        inheritedColumns.add(inheritedColumn)
    }

    /**
     * Finds inheritedColumn according to specified name `name`.
     *
     * @param name name of the inheritedColumn to be searched
     *
     * @return found inheritedColumn or null if no such inheritedColumn
     * has been found
     */
    override fun getInheritedColumn(name: String?): PgInheritedColumn? {
        if (inherits.isNotEmpty()) {
            for (inheritedColumn in inheritedColumns) {
                if (inheritedColumn.inheritedColumn.name == name) {
                    return inheritedColumn
                }
            }
        }
        return null
    }

    /**
     * Adds `constraint` to the list of constraints.
     *
     * @param constraint constraint
     */
    fun addConstraint(constraint: PgConstraint) {
        constraints.add(constraint)
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
    override fun containsInheritedColumn(name: String?): Boolean {
        if (inherits.isNotEmpty()) {
            for (inheritedColumn in inheritedColumns) {
                if (inheritedColumn.inheritedColumn.name == name) {
                    return true
                }
            }
        }
        return false
    }

    /**
     * Returns true if table contains given constraint `name`, otherwise
     * false.
     *
     * @param name name of the constraint
     *
     * @return true if table contains given constraint `name`, otherwise
     * false
     */
    fun containsConstraint(name: String?): Boolean {
        for (constraint in constraints) {
            if (constraint.name == name) {
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

    fun hasRLSEnabled(): Boolean? {
        return rlsEnabled
    }

    fun setRLSEnabled(rlsEnabled: Boolean?) {
        this.rlsEnabled = rlsEnabled
    }

    fun hasRLSForced(): Boolean? {
        return rlsForced
    }

    fun setRLSForced(rlsForced: Boolean?) {
        this.rlsForced = rlsForced
    }

    fun addPolicy(policy: PgPolicy) {
        policies.add(policy)
    }

    fun getPolicy(name: String?): PgPolicy? {
        for (policy in policies) {
            if (policy.name == name) {
                return policy
            }
        }
        return null
    }

}

class PgTable(name: String, database: PgDatabase, schema: PgSchema, position: Int) : PgTableBase(
    name, "TABLE", database, schema,
    position
)

class PgForeignTable(name: String, database: PgDatabase, schema: PgSchema, position: Int) :
    PgTableBase(name, "FOREIGN TABLE", database, schema, position)