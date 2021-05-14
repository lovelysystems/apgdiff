/**
 * Copyright 2006 StartNet s.r.o.
 *
 * Distributed under MIT license
 */
package cz.startnet.utils.pgdiff

import cz.startnet.utils.pgdiff.schema.PgIndex
import cz.startnet.utils.pgdiff.schema.PgSchema
import cz.startnet.utils.pgdiff.schema.PgTable
import cz.startnet.utils.pgdiff.schema.PgTableBase
import java.io.PrintWriter
import java.util.*

/**
 * Diffs indexes.
 *
 * @author fordfrog
 */
object PgDiffIndexes {
    /**
     * Outputs statements for creation of new indexes.
     *
     * @param writer           writer the output should be written to
     * @param oldSchema        original schema
     * @param newSchema        new schema
     * @param searchPathHelper search path helper
     */
    fun createIndexes(
        writer: PrintWriter,
        oldSchema: PgSchema?, newSchema: PgSchema?,
        searchPathHelper: SearchPathHelper
    ) {
        for (newTable in newSchema?.tables.orEmpty()) {
            val newTableName = newTable.name

            // Add new indexes
            if (oldSchema == null) {
                for (index in newTable.indexes) {
                    searchPathHelper.outputSearchPath(writer)
                    writer.println()
                    writer.println(index.creationSQL)
                }
            } else {
                for (index in getNewIndexes(
                    oldSchema.getTable(newTableName), newTable
                )) {
                    searchPathHelper.outputSearchPath(writer)
                    writer.println()
                    writer.println(index.creationSQL)
                }
            }
        }
    }

    /**
     * Outputs statements for dropping indexes that exist no more.
     *
     * @param writer           writer the output should be written to
     * @param oldSchema        original schema
     * @param newSchema        new schema
     * @param searchPathHelper search path helper
     */
    fun dropIndexes(
        writer: PrintWriter,
        oldSchema: PgSchema?, newSchema: PgSchema?,
        searchPathHelper: SearchPathHelper
    ) {
        for (newTable in newSchema?.tables.orEmpty()) {
            val newTableName = newTable.name
            val oldTable = oldSchema?.getTable(newTableName)

            // Drop indexes that do not exist in new schema or are modified
            for (index in getDropIndexes(oldTable, newTable)) {
                searchPathHelper.outputSearchPath(writer)
                writer.println()
                writer.println(index.dropSQL)
            }
        }
    }

    /**
     * Returns list of indexes that should be dropped.
     *
     * @param oldTable original table
     * @param newTable new table
     *
     * @return list of indexes that should be dropped
     *
     * @todo Indexes that are depending on a removed field should not be added
     * to drop because they are already removed.
     */
    private fun getDropIndexes(
        oldTable: PgTableBase?,
        newTable: PgTableBase?
    ): List<PgIndex> {
        val list: MutableList<PgIndex> = ArrayList()
        if (newTable != null && oldTable != null) {
            for (index in oldTable.indexes) {
                if (!newTable.containsIndex(index.name)
                    || newTable.getIndex(index.name) != index
                ) {
                    list.add(index)
                }
            }
        }
        return list
    }

    /**
     * Returns list of indexes that should be added.
     *
     * @param oldTable original table
     * @param newTable new table
     *
     * @return list of indexes that should be added
     */
    private fun getNewIndexes(
        oldTable: PgTableBase?,
        newTable: PgTableBase?
    ): List<PgIndex> {
        val list: MutableList<PgIndex> = ArrayList()
        if (newTable != null) {
            if (oldTable == null) {
                for (index in newTable.indexes) {
                    list.add(index)
                }
            } else {
                for (index in newTable.indexes) {
                    if (!oldTable.containsIndex(index.name)
                        || oldTable.getIndex(index.name) != index
                    ) {
                        list.add(index)
                    }
                }
            }
        }
        return list
    }

    /**
     * Outputs statements for index comments that have changed.
     *
     * @param writer           writer
     * @param oldSchema        old schema
     * @param newSchema        new schema
     * @param searchPathHelper search path helper
     */
    fun alterComments(
        writer: PrintWriter,
        oldSchema: PgSchema?, newSchema: PgSchema?,
        searchPathHelper: SearchPathHelper
    ) {
        if (oldSchema == null) {
            return
        }
        for (oldIndex in oldSchema.indexes) {
            val newIndex = newSchema!!.getIndex(oldIndex.name) ?: continue
            if (oldIndex.comment == null
                && newIndex.comment != null
                || oldIndex.comment != null && newIndex.comment != null && oldIndex.comment != newIndex.comment
            ) {
                searchPathHelper.outputSearchPath(writer)
                writer.println()
                writer.print("COMMENT ON INDEX ")
                writer.print(
                    PgDiffUtils.getQuotedName(newIndex.name)
                )
                writer.print(" IS ")
                writer.print(newIndex.comment)
                writer.println(';')
            } else if (oldIndex.comment != null
                && newIndex.comment == null
            ) {
                searchPathHelper.outputSearchPath(writer)
                writer.println()
                writer.print("COMMENT ON INDEX ")
                writer.print(
                    PgDiffUtils.getQuotedName(newIndex.name)
                )
                writer.println(" IS NULL;")
            }
        }
    }
}