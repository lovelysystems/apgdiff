/**
 * Copyright 2006 StartNet s.r.o.
 *
 * Distributed under MIT license
 */
package cz.startnet.utils.pgdiff

import cz.startnet.utils.pgdiff.schema.*
import java.io.PrintWriter
import java.text.MessageFormat

/**
 * Diffs tables.
 *
 * @author fordfrog
 */
object PgDiffTables {
    /**
     * Outputs statements for creation of clusters.
     *
     * @param writer           writer the output should be written to
     * @param oldSchema        original schema
     * @param newSchema        new schema
     */
    fun dropClusters(
        writer: PrintWriter,
        oldSchema: PgSchema?, newSchema: PgSchema?
    ) {
        for (newTable in newSchema?.tables.orEmpty()) {
            val oldTable = oldSchema?.getTable(newTable.name)
            val oldCluster = oldTable?.clusterIndexName
            val newCluster = newTable.clusterIndexName
            if (oldCluster != null && newCluster == null && newTable.containsIndex(oldCluster)) {
                writer.println()
                writer.print("ALTER TABLE ")
                writer.print(PgDiffUtils.getQuotedName(newTable.name))
                writer.println(" SET WITHOUT CLUSTER;")
            }
        }
    }

    /**
     * Outputs statements for dropping of clusters.
     *
     * @param writer           writer the output should be written to
     * @param oldSchema        original schema
     * @param newSchema        new schema
     */
    fun createClusters(
        writer: PrintWriter,
        oldSchema: PgSchema?, newSchema: PgSchema?
    ) {
        for (newTable in newSchema?.tables.orEmpty()) {
            val oldTable = oldSchema?.getTable(newTable.name)
            val oldCluster = oldTable?.clusterIndexName
            val newCluster = newTable.clusterIndexName
            if (oldCluster == null && newCluster != null
                || oldCluster != null && newCluster != null && newCluster.compareTo(oldCluster) != 0
            ) {
                writer.println()
                writer.print("ALTER TABLE ")
                writer.print(PgDiffUtils.getQuotedName(newTable.name))
                writer.print(" CLUSTER ON ")
                writer.print(PgDiffUtils.getQuotedName(newCluster))
                writer.println(';')
            }
        }
    }

    /**
     * Outputs statements for altering tables.
     *
     * @param writer           writer the output should be written to
     * @param arguments        object containing arguments settings
     * @param oldSchema        original schema
     * @param newSchema        new schema
     */
    fun alterTables(
        writer: PrintWriter,
        arguments: PgDiffOptions, oldSchema: PgSchema?,
        newSchema: PgSchema
    ) {
        for (newTable in newSchema.tables) {
            if (oldSchema == null
                || !oldSchema.containsTable(newTable.name)
            ) {
                continue
            }
            val oldTable = oldSchema.getTable(newTable.name)
            updateTableColumns(
                writer, arguments, oldTable, newTable
            )
            checkInherits(writer, oldTable, newTable, newSchema)
            addInheritedColumnDefaults(writer, oldTable, newTable)
            checkTablespace(writer, oldTable, newTable)
            addAlterStatistics(writer, oldTable, newTable)
            addAlterGenerated(writer, oldTable, newTable)
            addAlterStorage(writer, oldTable, newTable)
            alterComments(writer, oldTable, newTable)
            alterOwnerTo(writer, oldTable, newTable)
            alterPrivileges(writer, oldTable, newTable)
            alterPrivilegesColumns(writer, oldTable, newTable)
            alterRLS(writer, oldTable, newTable)

        }
    }

    /**
     * Generate the needed alter table xxx set statistics when needed.
     *
     * @param writer           writer the output should be written to
     * @param oldTable         original table
     * @param newTable         new table
     */
    private fun addAlterStatistics(
        writer: PrintWriter,
        oldTable: PgTableBase?, newTable: PgTableBase?
    ) {
        val stats: MutableMap<String?, Int> = HashMap()
        for (newColumn in newTable?.columns.orEmpty()) {
            val oldColumn = oldTable!!.getColumn(newColumn.name)
            if (oldColumn != null) {
                val oldStat = oldColumn.statistics
                val newStat = newColumn.statistics
                var newStatValue: Int? = null
                if (newStat != null && (oldStat == null
                            || newStat != oldStat)
                ) {
                    newStatValue = newStat
                } else if (oldStat != null && newStat == null) {
                    newStatValue = Integer.valueOf(-1)
                }
                if (newStatValue != null) {
                    stats[newColumn.name] = newStatValue
                }
            }
        }
        for ((key, value) in stats) {
            writer.println()
            writer.print("ALTER TABLE ONLY ")
            writer.print(PgDiffUtils.getQuotedName(newTable!!.name))
            writer.print(" ALTER COLUMN ")
            writer.print(PgDiffUtils.getQuotedName(key))
            writer.print(" SET STATISTICS ")
            writer.print(value)
            writer.println(';')
        }
    }

    /**
     * Generate the needed alter table xxx add generated when needed
     */
    private fun addAlterGenerated(
        writer: PrintWriter,
        oldTable: PgTableBase?, newTable: PgTableBase
    ) {
        for (newColumn in newTable.columns) {
            val oldColumn = oldTable!!.getColumn(newColumn.name)
            val oldGenerated = oldColumn?.generated
            val newGenerated = newColumn.generated
            if (newGenerated == null && oldGenerated != null) {
                TODO("generated definition removed from column")
            }
            if (newGenerated == null || newGenerated == oldGenerated) {
                continue
            }
            writer.println()
            writer.print("ALTER TABLE ")
            writer.print(PgDiffUtils.getQuotedName(newTable.name))
            writer.print(" ALTER COLUMN ")
            writer.print(PgDiffUtils.getQuotedName(newColumn.name))
            newGenerated.sql(writer)
            writer.print(';')
        }
    }

    /**
     * Generate the needed alter table xxx set storage when needed.
     *
     * @param writer           writer the output should be written to
     * @param oldTable         original table
     * @param newTable         new table
     */
    private fun addAlterStorage(
        writer: PrintWriter,
        oldTable: PgTableBase?, newTable: PgTableBase
    ) {
        for (newColumn in newTable.columns) {
            val oldColumn = oldTable!!.getColumn(newColumn.name)
            val oldStorage = oldColumn?.storage
            val newStorage = if (newColumn.storage == null
                || newColumn.storage.isNullOrEmpty()
            ) null else newColumn.storage
            if (newStorage == null && oldStorage != null) {
                writer.println()
                writer.println(
                    MessageFormat.format(
                        Resources.getString(
                            "WarningUnableToDetermineStorageType"
                        ),
                        newTable.name + '.' + newColumn.name
                    )
                )
                continue
            }
            if (newStorage == null || newStorage.equals(oldStorage, ignoreCase = true)) {
                continue
            }
            writer.println()
            writer.print("ALTER TABLE ONLY ")
            writer.print(PgDiffUtils.getQuotedName(newTable.name))
            writer.print(" ALTER COLUMN ")
            writer.print(PgDiffUtils.getQuotedName(newColumn.name))
            writer.print(" SET STORAGE ")
            writer.print(newStorage)
            writer.print(';')
        }
    }

    /**
     * Adds statements for creation of new columns to the list of statements.
     *
     * @param statements          list of statements
     * @param arguments           object containing arguments settings
     * @param oldTable            original table
     * @param newTable            new table
     * @param dropDefaultsColumns list for storing columns for which default
     * value should be dropped
     */
    private fun addCreateTableColumns(
        statements: MutableList<String>,
        arguments: PgDiffOptions, oldTable: PgTableBase?,
        newTable: PgTableBase?, dropDefaultsColumns: MutableList<PgColumn>
    ) {
        for (column in newTable!!.columns) {
            if (!oldTable!!.containsColumn(column.name)) {
                statements.add(
                    "\tADD COLUMN " + PgDiffUtils.createIfNotExists
                            + column.getFullDefinition(arguments.isAddDefaults)
                )
                if (arguments.isAddDefaults && !column.nullValue
                    && (column.defaultValue.isNullOrEmpty())
                ) {
                    dropDefaultsColumns.add(column)
                }
            }
        }
    }

    /**
     * Adds statements for removal of columns to the list of statements.
     *
     * @param statements list of statements
     * @param oldTable   original table
     * @param newTable   new table
     */
    private fun addDropTableColumns(
        statements: MutableList<String>,
        oldTable: PgTableBase?, newTable: PgTableBase?
    ) {
        for (column in oldTable!!.columns) {
            if (!newTable!!.containsColumn(column.name)) {
                statements.add(
                    "\tDROP COLUMN " + PgDiffUtils.dropIfExists
                            + PgDiffUtils.getQuotedName(column.name)
                )
            }
        }
    }

    /**
     * Adds statements for modification of columns to the list of statements.
     *
     * @param statements          list of statements
     * @param arguments           object containing arguments settings
     * @param oldTable            original table
     * @param newTable            new table
     * @param dropDefaultsColumns list for storing columns for which default
     * value should be dropped
     */
    private fun addModifyTableColumns(
        statements: MutableList<String>,
        arguments: PgDiffOptions, oldTable: PgTableBase?,
        newTable: PgTableBase?, dropDefaultsColumns: MutableList<PgColumn>
    ) {
        for (newColumn in newTable!!.columns) {
            if (!oldTable!!.containsColumn(newColumn.name)) {
                continue
            }
            val oldColumn = oldTable.getColumn(newColumn.name)!!
            val newColumnName = PgDiffUtils.getQuotedName(newColumn.name)
            if (oldColumn.type != newColumn.type) {
                val using = when (newTable) {
                    is PgTable -> " USING " + newColumnName + "::" + newColumn.type
                    is PgForeignTable -> ""
                }
                statements.add(
                    "\tALTER COLUMN " + newColumnName + " TYPE "
                            + newColumn.type + using
                )
            }
            val oldDefault = oldColumn.defaultValue.orEmpty()
            val newDefault = newColumn.defaultValue.orEmpty()
            if (oldDefault != newDefault) {
                if (newDefault.isEmpty()) {
                    statements.add(
                        "\tALTER COLUMN " + newColumnName
                                + " DROP DEFAULT"
                    )
                } else {
                    statements.add(
                        "\tALTER COLUMN " + newColumnName
                                + " SET DEFAULT " + newDefault
                    )
                }
            }
            if (oldColumn.nullValue != newColumn.nullValue) {
                if (newColumn.nullValue) {
                    statements.add(
                        "\tALTER COLUMN " + newColumnName
                                + " DROP NOT NULL"
                    )
                } else {
                    if (arguments.isAddDefaults) {
                        val defaultValue = PgColumnUtils.getDefaultValue(
                            newColumn.type
                        )
                        if (defaultValue != null) {
                            statements.add(
                                "\tALTER COLUMN " + newColumnName
                                        + " SET DEFAULT " + defaultValue
                            )
                            dropDefaultsColumns.add(newColumn)
                        }
                    }
                    statements.add(
                        "\tALTER COLUMN " + newColumnName
                                + " SET NOT NULL"
                    )
                }
            }
        }
    }

    /**
     * Checks whether there is a discrepancy in INHERITS for original and new
     * table.
     *
     * @param writer           writer the output should be written to
     * @param oldTable         original table
     * @param newTable         new table
     * @param newSchema        new schema
     */
    private fun checkInherits(
        writer: PrintWriter,
        oldTable: PgTableBase?,
        newTable: PgTableBase?,
        newSchema: PgSchema?
    ) {
        for (inheritPairN in newTable!!.inherits) {
            val schemaName = inheritPairN.first
            val tableName = inheritPairN.second
            var isFound = false
            for (inheritPairO in oldTable!!.inherits) {
                if (schemaName == inheritPairO.first && tableName == inheritPairO.second) {
                    isFound = true
                    break
                }
            }
            if (!isFound) {
                val inheritTableName: String = if (newSchema?.name == schemaName) {
                    PgDiffUtils.getQuotedName(tableName)
                } else {
                    String.format("%s.%s", PgDiffUtils.getQuotedName(schemaName), PgDiffUtils.getQuotedName(tableName))
                }
                writer.println()
                writer.println(
                    "ALTER TABLE "
                            + PgDiffUtils.getQuotedName(newTable.name)
                )
                writer.println(
                    "\tINHERIT "
                            + inheritTableName + ';'
                )
            }
        }
        for (inheritPairO in oldTable!!.inherits) {
            val schemaName = inheritPairO.first
            val tableName = inheritPairO.second
            var isFound = false
            for (inheritPairN in newTable.inherits) {
                if (schemaName == inheritPairN.first && tableName == inheritPairN.second) {
                    isFound = true
                    break
                }
            }
            if (!isFound) {
                val inheritTableName: String = if (newSchema?.name == schemaName) {
                    PgDiffUtils.getQuotedName(tableName)
                } else {
                    String.format("%s.%s", PgDiffUtils.getQuotedName(schemaName), PgDiffUtils.getQuotedName(tableName))
                }
                writer.println()
                writer.println(
                    "ALTER TABLE "
                            + PgDiffUtils.getQuotedName(newTable.name)
                )
                writer.println(
                    "\tNO INHERIT "
                            + inheritTableName + ';'
                )
            }
        }

    }

    /**
     * Outputs statements for defaults of tables who's column belongs to
     * an inherited table.
     *
     * @param writer           writer the output should be written to
     * @param oldTable         original table
     * @param newTable         new table
     */
    private fun addInheritedColumnDefaults(
        writer: PrintWriter,
        oldTable: PgTableBase?, newTable: PgTableBase?
    ) {
        for (newColumn in newTable!!.inheritedColumns) {
            if (!oldTable!!.containsInheritedColumn(newColumn.inheritedColumn.name)) {
                continue
            }
            val oldColumn = oldTable.getInheritedColumn(newColumn.inheritedColumn.name)
            val oldDefault = oldColumn?.defaultValue.orEmpty()
            val newDefault = newColumn.defaultValue.orEmpty()
            if (oldDefault != newDefault) {
                writer.println()
                writer.print("ALTER TABLE ONLY ")
                writer.println(PgDiffUtils.getQuotedName(newTable.name))
                writer.print("\tALTER COLUMN ")
                writer.print(PgDiffUtils.getQuotedName(newColumn.inheritedColumn.name))
                if (newDefault.isEmpty()) {
                    writer.print(" DROP DEFAULT")
                } else {
                    writer.print(" SET DEFAULT ")
                    writer.print(newDefault)
                }
                writer.println(";")
            }
        }
    }

    /**
     * Checks tablespace modification.
     *
     * @param writer           writer
     * @param oldTable         old table
     * @param newTable         new table
     */
    private fun checkTablespace(
        writer: PrintWriter,
        oldTable: PgTableBase?, newTable: PgTableBase
    ) {
        if (oldTable?.tablespace == null && newTable.tablespace == null
            || oldTable?.tablespace != null
            && oldTable.tablespace == newTable.tablespace
        ) {
            return
        }
        writer.println()
        writer.println(
            "ALTER TABLE "
                    + PgDiffUtils.getQuotedName(newTable.name)
        )
        writer.println("\tTABLESPACE " + newTable.tablespace + ';')
    }

    /**
     * Outputs statements for creation of new tables.
     *
     * @param writer           writer the output should be written to
     * @param oldSchema        original schema
     * @param newSchema        new schema
     */
    fun createTables(
        writer: PrintWriter,
        oldSchema: PgSchema?, newSchema: PgSchema
    ) {
        for (table in newSchema.tables) {
            if (oldSchema == null
                || !oldSchema.containsTable(table.name)
            ) {
                writer.println()
                writer.println(table.getCreationSQL(newSchema))
                writer.println()
                if (table.owner != null) {
                    table.ownerSQL(writer)
                }
                for (tablePrivilege in table.privileges) {
                    writer.println(
                        "REVOKE ALL ON TABLE "
                                + PgDiffUtils.getQuotedName(table.name)
                                + " FROM " + tablePrivilege.roleName + ";"
                    )
                    if ("" != tablePrivilege.getPrivilegesSQL(true)) {
                        writer.println(
                            "GRANT "
                                    + tablePrivilege.getPrivilegesSQL(true)
                                    + " ON TABLE "
                                    + PgDiffUtils.getQuotedName(table.name)
                                    + " TO " + tablePrivilege.roleName
                                    + " WITH GRANT OPTION;"
                        )
                    }
                    if ("" != tablePrivilege.getPrivilegesSQL(false)) {
                        writer.println(
                            "GRANT "
                                    + tablePrivilege.getPrivilegesSQL(false)
                                    + " ON TABLE "
                                    + PgDiffUtils.getQuotedName(table.name)
                                    + " TO " + tablePrivilege.roleName + ";"
                        )
                    }
                }
                if (table.hasRLSEnabled() != null && table.hasRLSEnabled()!!) {
                    writer.println(
                        "ALTER TABLE "
                                + PgDiffUtils.getQuotedName(table.name)
                                + "  ENABLE ROW LEVEL SECURITY;"
                    )
                }
                if (table.hasRLSForced() != null && table.hasRLSForced()!!) {
                    writer.println(
                        "ALTER TABLE "
                                + PgDiffUtils.getQuotedName(table.name)
                                + "  FORCE ROW LEVEL SECURITY;"
                    )
                }
            }
        }
    }

    /**
     * Outputs statements for addition, removal and modifications of table
     * columns.
     *
     * @param writer           writer the output should be written to
     * @param arguments        object containing arguments settings
     * @param oldTable         original table
     * @param newTable         new table
     */
    private fun updateTableColumns(
        writer: PrintWriter,
        arguments: PgDiffOptions, oldTable: PgTableBase?,
        newTable: PgTableBase
    ) {
        val statements: MutableList<String> = ArrayList()
        val dropDefaultsColumns: MutableList<PgColumn> = ArrayList()
        addDropTableColumns(statements, oldTable, newTable)
        addCreateTableColumns(
            statements, arguments, oldTable, newTable, dropDefaultsColumns
        )
        addModifyTableColumns(
            statements, arguments, oldTable, newTable, dropDefaultsColumns
        )
        if (statements.isNotEmpty()) {
            val quotedTableName = PgDiffUtils.getQuotedName(newTable.name)
            writer.println()
            writer.println("ALTER ${newTable.objectType} $quotedTableName")
            for (i in statements.indices) {
                writer.print(statements[i])
                writer.println(if (i + 1 < statements.size) "," else ";")
            }
            if (dropDefaultsColumns.isNotEmpty()) {
                writer.println()
                writer.println("ALTER ${newTable.objectType} $quotedTableName")
                for (i in dropDefaultsColumns.indices) {
                    writer.print("\tALTER COLUMN ")
                    writer.print(
                        PgDiffUtils.getQuotedName(
                            dropDefaultsColumns[i].name
                        )
                    )
                    writer.print(" DROP DEFAULT")
                    writer.println(
                        if (i + 1 < dropDefaultsColumns.size) "," else ";"
                    )
                }
            }
        }
    }

    private fun alterPrivilegesColumns(
        writer: PrintWriter,
        oldTable: PgTableBase?, newTable: PgTableBase
    ) {
        var emptyLinePrinted = false
        for (newColumn in newTable.columns) {
            val oldColumn = oldTable!!.getColumn(newColumn.name)
            if (oldColumn != null) {
                for (oldColumnPrivilege in oldColumn.privileges) {
                    val newColumnPrivilege = newColumn
                        .getPrivilege(oldColumnPrivilege.roleName)
                    if (newColumnPrivilege == null) {
                        if (!emptyLinePrinted) {
                            emptyLinePrinted = true
                            writer.println()
                        }
                        writer.println(
                            "REVOKE ALL ("
                                    + PgDiffUtils.getQuotedName(newColumn.name)
                                    + ") ON TABLE "
                                    + PgDiffUtils.getQuotedName(newTable.name)
                                    + " FROM " + oldColumnPrivilege.roleName
                                    + ";"
                        )
                    }
                }
            }
            for (newColumnPrivilege in newColumn
                .privileges) {
                var oldColumnPrivilege: PgColumnPrivilege? = null
                if (oldColumn != null) {
                    oldColumnPrivilege = oldColumn
                        .getPrivilege(newColumnPrivilege.roleName)
                }
                if (!newColumnPrivilege.isSimilar(oldColumnPrivilege)) {
                    if (!emptyLinePrinted) {
                        emptyLinePrinted = true
                        writer.println()
                    }
                    writer.println(
                        "REVOKE ALL ("
                                + PgDiffUtils.getQuotedName(newColumn.name)
                                + ") ON TABLE "
                                + PgDiffUtils.getQuotedName(newTable.name)
                                + " FROM " + newColumnPrivilege.roleName
                                + ";"
                    )
                    if ("" != newColumnPrivilege.getPrivilegesSQL(
                            true,
                            PgDiffUtils.getQuotedName(newColumn.name)
                        )
                    ) {
                        writer.println(
                            "GRANT "
                                    + newColumnPrivilege.getPrivilegesSQL(
                                true,
                                PgDiffUtils.getQuotedName(
                                    newColumn
                                        .name
                                )
                            )
                                    + " ON TABLE "
                                    + PgDiffUtils.getQuotedName(
                                newTable
                                    .name
                            ) + " TO "
                                    + newColumnPrivilege.roleName
                                    + " WITH GRANT OPTION;"
                        )
                    }
                    if ("" != newColumnPrivilege.getPrivilegesSQL(
                            false,
                            PgDiffUtils.getQuotedName(newColumn.name)
                        )
                    ) {
                        writer.println(
                            "GRANT "
                                    + newColumnPrivilege.getPrivilegesSQL(
                                false, PgDiffUtils.getQuotedName(
                                    newColumn
                                        .name
                                )
                            )
                                    + " ON TABLE "
                                    + PgDiffUtils.getQuotedName(
                                newTable
                                    .name
                            ) + " TO "
                                    + newColumnPrivilege.roleName + ";"
                        )
                    }
                }
            }
        }
    }

    /**
     * Outputs statements for tables and columns for which comments have
     * changed.
     *
     * @param writer           writer
     * @param oldTable         old table
     * @param newTable         new table
     */
    private fun alterComments(
        writer: PrintWriter,
        oldTable: PgTableBase?, newTable: PgTableBase
    ) {
        if (oldTable?.comment == null
            && newTable.comment != null
            || oldTable?.comment != null && newTable.comment != null && oldTable.comment != newTable.comment
        ) {
            writer.println()
            writer.print("COMMENT ON TABLE ")
            writer.print(PgDiffUtils.getQuotedName(newTable.name))
            writer.print(" IS ")
            writer.print(newTable.comment)
            writer.println(';')
        } else if (oldTable?.comment != null
            && newTable.comment == null
        ) {
            writer.println()
            writer.print("COMMENT ON TABLE ")
            writer.print(PgDiffUtils.getQuotedName(newTable.name))
            writer.println(" IS NULL;")
        }

        for (newColumn in newTable.inheritedColumns) {
            val oldComment = oldTable?.getInheritedColumn(newColumn.name)?.comment
            if (newColumn.comment != oldComment) {
                newColumn.commentSQL(writer)
            }
        }

        for (newColumn in newTable.columns) {
            val oldComment = oldTable?.getColumn(newColumn.name)?.comment
            if (newColumn.comment != oldComment) {
                newColumn.commentSQL(writer)
            }
        }

    }

    private fun alterPrivileges(
        writer: PrintWriter,
        oldTable: PgTableBase?, newTable: PgTableBase
    ) {
        var emptyLinePrinted = false
        for (oldTablePrivilege in oldTable!!.privileges) {
            val newTablePrivilege = newTable.getPrivilege(oldTablePrivilege.roleName)
            if (newTablePrivilege == null) {
                if (!emptyLinePrinted) {
                    emptyLinePrinted = true
                    writer.println()
                }
                writer.println(
                    "REVOKE ALL ON TABLE "
                            + PgDiffUtils.getQuotedName(oldTable.name)
                            + " FROM " + oldTablePrivilege.roleName + ";"
                )
            } else if (!oldTablePrivilege.isSimilar(newTablePrivilege)) {
                if (!emptyLinePrinted) {
                    emptyLinePrinted = true
                    writer.println()
                }
                writer.println(
                    "REVOKE ALL ON TABLE "
                            + PgDiffUtils.getQuotedName(newTable.name)
                            + " FROM " + newTablePrivilege.roleName + ";"
                )
                if ("" != newTablePrivilege.getPrivilegesSQL(true)) {
                    writer.println(
                        "GRANT "
                                + newTablePrivilege.getPrivilegesSQL(true)
                                + " ON TABLE "
                                + PgDiffUtils.getQuotedName(newTable.name)
                                + " TO " + newTablePrivilege.roleName
                                + " WITH GRANT OPTION;"
                    )
                }
                if ("" != newTablePrivilege.getPrivilegesSQL(false)) {
                    writer.println(
                        "GRANT "
                                + newTablePrivilege.getPrivilegesSQL(false)
                                + " ON TABLE "
                                + PgDiffUtils.getQuotedName(newTable.name)
                                + " TO " + newTablePrivilege.roleName + ";"
                    )
                }
            } // else similar privilege will not be updated
        }
        for (newTablePrivilege in newTable.privileges) {
            val oldTablePrivilege = oldTable
                .getPrivilege(newTablePrivilege.roleName)
            if (oldTablePrivilege == null) {
                if (!emptyLinePrinted) {
                    writer.println()
                }
                writer.println(
                    "REVOKE ALL ON TABLE "
                            + PgDiffUtils.getQuotedName(newTable.name)
                            + " FROM " + newTablePrivilege.roleName + ";"
                )
                if ("" != newTablePrivilege.getPrivilegesSQL(true)) {
                    writer.println(
                        "GRANT "
                                + newTablePrivilege.getPrivilegesSQL(true)
                                + " ON TABLE "
                                + PgDiffUtils.getQuotedName(newTable.name)
                                + " TO " + newTablePrivilege.roleName
                                + " WITH GRANT OPTION;"
                    )
                }
                if ("" != newTablePrivilege.getPrivilegesSQL(false)) {
                    writer.println(
                        "GRANT "
                                + newTablePrivilege.getPrivilegesSQL(false)
                                + " ON TABLE "
                                + PgDiffUtils.getQuotedName(newTable.name)
                                + " TO " + newTablePrivilege.roleName + ";"
                    )
                }
            }
        }
    }

    private fun alterOwnerTo(
        writer: PrintWriter,
        oldTable: PgTableBase?, newTable: PgTableBase
    ) {
        val oldOwnerTo = oldTable?.owner
        val newOwnerTo = newTable.owner
        if (newOwnerTo != null && newOwnerTo != oldOwnerTo) {
            newTable.ownerSQL(writer)
        }
    }

    private fun alterRLS(
        writer: PrintWriter,
        oldTable: PgTableBase?, newTable: PgTableBase
    ) {
        if ((oldTable!!.hasRLSEnabled() == null || oldTable.hasRLSEnabled() != null && !oldTable.hasRLSEnabled()!!)
            && newTable.hasRLSEnabled() != null && newTable.hasRLSEnabled()!!
        ) {
            writer.println()
            writer.print("ALTER TABLE ")
            writer.print(PgDiffUtils.getQuotedName(newTable.name))
            writer.println(" ENABLE ROW LEVEL SECURITY;")
        }
        if (oldTable.hasRLSEnabled() != null && oldTable.hasRLSEnabled()!!
            && (newTable.hasRLSEnabled() == null || newTable.hasRLSEnabled() != null && !newTable.hasRLSEnabled()!!)
        ) {
            writer.println()
            writer.print("ALTER TABLE ")
            writer.print(PgDiffUtils.getQuotedName(newTable.name))
            writer.println(" DISABLE ROW LEVEL SECURITY;")
        }
        if ((oldTable.hasRLSForced() == null || oldTable.hasRLSForced() != null && !oldTable.hasRLSForced()!!)
            && newTable.hasRLSForced() != null && newTable.hasRLSForced()!!
        ) {
            writer.println()
            writer.print("ALTER TABLE ")
            writer.print(PgDiffUtils.getQuotedName(newTable.name))
            writer.println(" FORCE ROW LEVEL SECURITY;")
        }
        if (oldTable.hasRLSForced() != null && oldTable.hasRLSForced()!!
            && (newTable.hasRLSForced() == null || newTable.hasRLSForced() != null && !newTable.hasRLSForced()!!)
        ) {
            writer.println()
            writer.print("ALTER TABLE ")
            writer.print(PgDiffUtils.getQuotedName(newTable.name))
            writer.println(" NO FORCE ROW LEVEL SECURITY;")
        }
    }
}