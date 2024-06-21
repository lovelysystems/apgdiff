/**
 * Copyright 2006 StartNet s.r.o.
 *
 * Distributed under MIT license
 */
package cz.startnet.utils.pgdiff

import cz.startnet.utils.pgdiff.schema.PgConstraint
import cz.startnet.utils.pgdiff.schema.PgSchema
import cz.startnet.utils.pgdiff.schema.PgTableBase
import kotlin.text.StringBuilder

/**
 * Diffs constraints.
 *
 * @author fordfrog
 */
object PgDiffConstraints {
    /**
     * Outputs statements for creation of new constraints.
     *
     * @param writer           writer the output should be written to
     * @param oldSchema        original schema
     * @param newSchema        new schema
     * @param primaryKey       determines whether primary keys should be
     * processed or any other constraints should be
     * processed
     */
    fun createConstraints(
        writer: StringBuilder,
        oldSchema: PgSchema?, newSchema: PgSchema,
        primaryKey: Boolean
    ) {
        for (newTable in newSchema.tables) {
            val oldTable = oldSchema?.getTable(newTable.name)

            // Add new constraints
            for (constraint in getNewConstraints(oldTable, newTable, primaryKey)) {
                writer.println()
                writer.appendLine(constraint.creationSQL)
            }
        }
    }

    /**
     * Outputs statements for dropping non-existent or modified constraints.
     *
     * @param writer           writer the output should be written to
     * @param oldSchema        original schema
     * @param newSchema        new schema
     * @param primaryKey       determines whether primary keys should be
     * processed or any other constraints should be
     * processed
     */
    fun dropConstraints(
        writer: StringBuilder,
        oldSchema: PgSchema?, newSchema: PgSchema,
        primaryKey: Boolean
    ) {
        for (newTable in newSchema.tables) {
            val oldTable = oldSchema?.getTable(newTable.name)

            // Drop constraints that no more exist or are modified
            for (constraint in getDropConstraints(oldTable, newTable, primaryKey)) {
                writer.println()
                writer.appendLine(constraint.dropSQL)
            }
        }
    }

    /**
     * Returns list of constraints that should be dropped.
     *
     * @param oldTable   original table or null
     * @param newTable   new table or null
     * @param primaryKey determines whether primary keys should be processed or
     * any other constraints should be processed
     *
     * @return list of constraints that should be dropped
     *
     * @todo Constraints that are depending on a removed field should not be
     * added to drop because they are already removed.
     */
    private fun getDropConstraints(
        oldTable: PgTableBase?,
        newTable: PgTableBase?, primaryKey: Boolean
    ): List<PgConstraint> {
        val list: MutableList<PgConstraint> = ArrayList()
        if (newTable != null && oldTable != null) {
            for (constraint in oldTable.constraints) {
                if (constraint.isPrimaryKeyConstraint == primaryKey
                    && (!newTable.containsConstraint(constraint.name)
                            || newTable.getConstraint(constraint.name) != constraint)
                ) {
                    list.add(constraint)
                }
            }
        }
        return list
    }

    /**
     * Returns list of constraints that should be added.
     *
     * @param oldTable   original table
     * @param newTable   new table
     * @param primaryKey determines whether primary keys should be processed or
     * any other constraints should be processed
     *
     * @return list of constraints that should be added
     */
    private fun getNewConstraints(
        oldTable: PgTableBase?,
        newTable: PgTableBase?, primaryKey: Boolean
    ): List<PgConstraint> {
        val list: MutableList<PgConstraint> = ArrayList()
        if (newTable != null) {
            if (oldTable == null) {
                for (constraint in newTable.constraints) {
                    if (constraint.isPrimaryKeyConstraint == primaryKey) {
                        list.add(constraint)
                    }
                }
            } else {
                for (constraint in newTable.constraints) {
                    if (constraint.isPrimaryKeyConstraint == primaryKey
                        && (!oldTable.containsConstraint(
                            constraint.name
                        )
                                || oldTable.getConstraint(constraint.name) != constraint)
                    ) {
                        list.add(constraint)
                    }
                }
            }
        }
        return list
    }

    /**
     * Outputs statements for constraint comments that have changed.
     *
     * @param writer           writer
     * @param oldSchema        old schema
     * @param newSchema        new schema
     */
    fun alterComments(
        writer: StringBuilder,
        oldSchema: PgSchema?, newSchema: PgSchema?
    ) {
        if (oldSchema == null) {
            return
        }
        for (oldTable in oldSchema.tables) {
            val newTable = newSchema!!.getTable(oldTable.name) ?: continue
            for (oldConstraint in oldTable.constraints) {
                val newConstraint = newTable.getConstraint(oldConstraint.name) ?: continue
                if (oldConstraint.comment == null
                    && newConstraint.comment != null
                    || oldConstraint.comment != null && newConstraint.comment != null && oldConstraint.comment != newConstraint.comment
                ) {
                    writer.println()
                    writer.append("COMMENT ON ")
                    if (newConstraint.isPrimaryKeyConstraint) {
                        writer.append("INDEX ")
                        writer.append(
                            PgDiffUtils.getQuotedName(
                                newConstraint.name
                            )
                        )
                    } else {
                        writer.append("CONSTRAINT ")
                        writer.append(
                            PgDiffUtils.getQuotedName(
                                newConstraint.name
                            )
                        )
                        writer.append(" ON ")
                        writer.append(
                            PgDiffUtils.getQuotedName(
                                newConstraint.tableName!!
                            )
                        )
                    }
                    writer.append(" IS ")
                    writer.print(newConstraint.comment)
                    writer.println(';')
                } else if (oldConstraint.comment != null
                    && newConstraint.comment == null
                ) {
                    writer.println()
                    writer.append("COMMENT ON ")
                    if (newConstraint.isPrimaryKeyConstraint) {
                        writer.append("INDEX ")
                        writer.append(
                            PgDiffUtils.getQuotedName(
                                newConstraint.name
                            )
                        )
                    } else {
                        writer.append("CONSTRAINT ")
                        writer.append(
                            PgDiffUtils.getQuotedName(
                                newConstraint.name
                            )
                        )
                        writer.append(" ON ")
                        writer.append(
                            PgDiffUtils.getQuotedName(
                                newConstraint.tableName!!
                            )
                        )
                    }
                    writer.appendLine(" IS NULL;")
                }
            }
        }
    }
}