/**
 * Copyright 2006 StartNet s.r.o.
 *
 * Distributed under MIT license
 */
package cz.startnet.utils.pgdiff.schema

import cz.startnet.utils.pgdiff.Pair
import cz.startnet.utils.pgdiff.PgDiffUtils
import java.util.*

/**
 * Stores table information.
 *
 * @author fordfrog
 */
class PgTable(name: String?, database: PgDatabase, schema: PgSchema) : PgRelation() {
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
    val inherits: MutableList<Pair<String?, String?>>? = ArrayList()

    /**
     * The with clause, currently not implemented
     */
    var with: String? = null

    /**
     * Is this a UNLOGGED table?
     */
    var isUnlogged = false

    /**
     * Is this a FOREIGN table?
     */
    var isForeign = false

    /**
     * Does this table have RLS enabled?
     */
    private var rlsEnabled: Boolean? = null

    /**
     * Does this table have RLS forced?
     */
    private var rlsForced: Boolean? = null
    var foreignServer: String? = null
    /**
     * Getter for [.rangePartition].
     *
     * @return [.rangePartition]
     */
    /**
     * Setter for [.rangePartition].
     *
     * @param rangePartition [.rangePartition]
     */
    var rangePartition: String? = null

    /**
     * RLS Policies
     */
    val policies: MutableList<PgPolicy> = ArrayList()

    /**
     * PgDatabase
     */
    private val database: PgDatabase

    /**
     * PgSchema
     */
    private val schema: PgSchema

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

//    /**
//     * Getter for [.constraints]. The list cannot be modified.
//     *
//     * @return [.constraints]
//     */
//    fun getConstraints(): List<PgConstraint> {
//        return Collections.unmodifiableList(constraints)
//    }

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
        if (isForeign) {
            sbSQL.append("FOREIGN ")
        }
        sbSQL.append("TABLE ")
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
        if (inherits != null && !inherits.isEmpty()) {
            sbSQL.append(System.getProperty("line.separator"))
            sbSQL.append("INHERITS (")
            first = true
            for (inheritPair in inherits) {
                if (first) {
                    first = false
                } else {
                    sbSQL.append(", ")
                }
                var inheritTableName: String? = null
                inheritTableName = if (schema.name == inheritPair.l) {
                    inheritPair.r
                } else {
                    String.format("%s.%s", inheritPair.l, inheritPair.r)
                }
                sbSQL.append(inheritTableName)
            }
            sbSQL.append(")")
        }
        if (with != null && !with!!.isEmpty()) {
            TODO("with clause in table creation not supported")
        }
        if (isForeign) {
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
    fun addInherits(schemaName: String?, tableName: String?) {
        inherits!!.add(Pair(schemaName, tableName))
        val inheritedTable = database.getSchema(schemaName)!!.getTable(tableName)
        for (column in inheritedTable!!.columns) {
            val inheritedColumn = PgInheritedColumn(column)
            inheritedColumns.add(inheritedColumn)
        }
        for (column in inheritedTable.inheritedColumns) {
            val inheritedColumn = PgInheritedColumn(column.inheritedColumn)
            inheritedColumns.add(inheritedColumn)
        }
    }

//    /**
//     * Getter for [.inherits].
//     *
//     * @return [.inherits]
//     */
//    fun getInherits(): List<Pair<String, String>> {
//        return Collections.unmodifiableList(inherits)
//    }
//    /**
//     * Getter for [.tablespace].
//     *
//     * @return [.tablespace]
//     */
//    /**
//     * Setter for [.tablespace].
//     *
//     * @param tablespace [.tablespace]
//     */
//    override var tablespace: String?
//        get() = super.tablespace
//        set(tablespace) {
//            this.tablespace = tablespace
//        }

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
        if (inherits != null && !inherits.isEmpty()) {
            for (inheritedColumn in inheritedColumns) {
                if (inheritedColumn.inheritedColumn.name == name) {
                    return inheritedColumn
                }
            }
        }
        return null
    }

//    /**
//     * Getter for [.inheritedColumns]. The list cannot be modified.
//     *
//     * @return [.inheritedColumns]
//     */
//    fun .inheritedColumns: List<PgInheritedColumn> {
//        return Collections.unmodifiableList(inheritedColumns)
//    }

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
        if (inherits != null && !inherits.isEmpty()) {
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
        private get() {
            val list: MutableList<PgColumn> = ArrayList()
            for (column in columns) {
                if (column.statistics != null) {
                    list.add(column)
                }
            }
            return list
        }

    /**
     * Foreign Tables
     */
    override val dropSQL: String
        get() = "DROP " + (if (isForeign) "FOREIGN " else "") + relationKind + " " + PgDiffUtils.dropIfExists +
                PgDiffUtils.getQuotedName(name) + ";"

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

//    fun getPolicies(): List<PgPolicy> {
//        return Collections.unmodifiableList(policies)
//    }

    /**
     * Creates a new PgTable object.
     *
     * @param name [.name]
     * @param database name of database
     * @param schema name of schema
     */
    init {
        this.name = name
        this.database = database
        this.schema = schema
    }
}