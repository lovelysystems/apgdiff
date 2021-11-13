/**
 * Copyright 2006 StartNet s.r.o.
 *
 * Distributed under MIT license
 */
package cz.startnet.utils.pgdiff

import cz.startnet.utils.pgdiff.schema.PgRelation
import cz.startnet.utils.pgdiff.schema.PgSchema
import cz.startnet.utils.pgdiff.schema.PgTrigger
import java.io.PrintWriter

/**
 * Diffs triggers.
 *
 * @author fordfrog
 */
object PgDiffTriggers {
    /**
     * Outputs statements for creation of new triggers.
     *
     * @param writer           writer the output should be written to
     * @param oldSchema        original schema
     * @param newSchema        new schema
     */
    fun createTriggers(
        writer: PrintWriter,
        oldSchema: PgSchema?, newSchema: PgSchema?
    ) {
        for (newRelation in newSchema!!.rels) {
            val oldRelation = oldSchema?.getRelation(newRelation.name)

            // Add new triggers
            for (trigger in getNewTriggers(oldRelation, newRelation)) {
                writer.println()
                writer.println(trigger.creationSQL)
            }
        }
    }

    /**
     * Outputs statements for dropping triggers.
     *
     * @param writer           writer the output should be written to
     * @param oldSchema        original schema
     * @param newSchema        new schema
     */
    fun dropTriggers(
        writer: PrintWriter,
        oldSchema: PgSchema?, newSchema: PgSchema?
    ) {
        for (newRelation in newSchema!!.rels) {
            val oldRelation = oldSchema?.getRelation(newRelation.name)

            // Drop triggers that no more exist or are modified
            for (trigger in getDropTriggers(oldRelation, newRelation)) {
                writer.println()
                writer.println(trigger.dropSQL)
            }
        }
    }

    /**
     * Returns list of triggers that should be dropped.
     *
     * @param oldRelation original relation
     * @param newRelation new relation
     *
     * @return list of triggers that should be dropped
     */
    private fun getDropTriggers(
        oldRelation: PgRelation<*, *>?,
        newRelation: PgRelation<*, *>?
    ): List<PgTrigger> {
        val list: MutableList<PgTrigger> = ArrayList()
        if (newRelation != null && oldRelation != null) {
            val newTriggers = newRelation.triggers
            for (oldTrigger in oldRelation.triggers) {
                if (!newTriggers.contains(oldTrigger)) {
                    list.add(oldTrigger)
                }
            }
        }
        return list
    }

    /**
     * Returns list of triggers that should be added.
     *
     * @param oldRelation original relation
     * @param newRelation new relation
     *
     * @return list of triggers that should be added
     */
    private fun getNewTriggers(
        oldRelation: PgRelation<*, *>?,
        newRelation: PgRelation<*, *>?
    ): List<PgTrigger> {
        val list: MutableList<PgTrigger> = ArrayList()
        if (newRelation != null) {
            if (oldRelation == null) {
                list.addAll(newRelation.triggers)
            } else {
                for (newTrigger in newRelation.triggers) {
                    if (!oldRelation.triggers.contains(newTrigger)) {
                        list.add(newTrigger)
                    }
                }
            }
        }
        return list
    }

    /**
     * Outputs statements for trigger comments that have changed.
     *
     * @param writer           writer
     * @param oldSchema        old schema
     * @param newSchema        new schema
     */
    fun alterComments(
        writer: PrintWriter,
        oldSchema: PgSchema?, newSchema: PgSchema?
    ) {
        if (oldSchema == null) {
            return
        }
        for (oldRelation in oldSchema.rels) {
            val newRelation = newSchema!!.getRelation(oldRelation.name) ?: continue
            for (oldTrigger in oldRelation.triggers) {
                val newTrigger = newRelation.getTrigger(oldTrigger.name) ?: continue
                if (oldTrigger.comment == null
                    && newTrigger.comment != null
                    || oldTrigger.comment != null && newTrigger.comment != null && oldTrigger.comment != newTrigger.comment
                ) {
                    writer.println()
                    writer.print("COMMENT ON TRIGGER ")
                    writer.print(
                        PgDiffUtils.getQuotedName(newTrigger.name)
                    )
                    writer.print(" ON ")
                    writer.print(
                        PgDiffUtils.getQuotedName(
                            newTrigger.relationName
                        )
                    )
                    writer.print(" IS ")
                    writer.print(newTrigger.comment)
                    writer.println(';')
                } else if (oldTrigger.comment != null
                    && newTrigger.comment == null
                ) {

                    writer.println()
                    writer.print("COMMENT ON TRIGGER ")
                    writer.print(
                        PgDiffUtils.getQuotedName(newTrigger.name)
                    )
                    writer.print(" ON ")
                    writer.print(
                        PgDiffUtils.getQuotedName(
                            newTrigger.relationName
                        )
                    )
                    writer.println(" IS NULL;")
                }
            }
        }
    }

    /**
     * Returns list of triggers that should be enable or disable.
     *
     * @param oldRelation original relation
     * @param newRelation new relation
     *
     * @return list of triggers that should be added
     */
    private fun getEnablerOrDisableTriggers(
        oldRelation: PgRelation<*, *>?,
        newRelation: PgRelation<*, *>?
    ): List<PgTrigger> {
        val list: MutableList<PgTrigger> = ArrayList()
        if (newRelation != null) {
            for (newTrigger in newRelation.triggers) {
                val oldTrigger = oldRelation?.getTrigger(newTrigger.name)
                if (newTrigger.isDisable && oldTrigger == null ||
                    oldTrigger != null && oldTrigger.isDisable != newTrigger.isDisable
                ) {
                    list.add(newTrigger)
                }
            }
        }
        return list
    }

    /**
     * Outputs statements for disable or enable triggers.
     *
     * @param writer           writer the output should be written to
     * @param oldSchema        original schema
     * @param newSchema        new schema
     */
    fun disableOrEnableTriggers(
        writer: PrintWriter,
        oldSchema: PgSchema?, newSchema: PgSchema?
    ) {
        for (newRelation in newSchema!!.rels) {
            val oldRelation = oldSchema?.getRelation(newRelation.name)

            // Add new triggers
            for (trigger in getEnablerOrDisableTriggers(oldRelation, newRelation)) {

                writer.println()
                writer.println(trigger.disableOrEnableSQL)
            }
        }
    }
}