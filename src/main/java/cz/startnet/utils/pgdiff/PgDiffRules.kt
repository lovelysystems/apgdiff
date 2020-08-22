/**
 * Copyright 2018 StartNet s.r.o.
 *
 * Distributed under MIT license
 */
package cz.startnet.utils.pgdiff

import cz.startnet.utils.pgdiff.schema.PgRelation
import cz.startnet.utils.pgdiff.schema.PgRule
import cz.startnet.utils.pgdiff.schema.PgSchema
import java.io.PrintWriter
import java.util.*

/**
 * Diffs rules.
 *
 * @author jalissonmello
 */
object PgDiffRules {
    /**
     * Outputs statements for creation of new triggers.
     *
     * @param writer           writer the output should be written to
     * @param oldSchema        original schema
     * @param newSchema        new schema
     * @param searchPathHelper search path helper
     */
    fun createRules(
        writer: PrintWriter,
        oldSchema: PgSchema?, newSchema: PgSchema?,
        searchPathHelper: SearchPathHelper
    ) {
        for (newRelation in newSchema?.rels.orEmpty()) {
            val oldRelation: PgRelation?
            oldRelation = oldSchema?.getRelation(newRelation.name)

            // Add new rules
            for (rule in getNewRules(oldRelation, newRelation)) {
                searchPathHelper.outputSearchPath(writer)
                writer.println()
                writer.println(rule.creationSQL)
            }
        }
    }

    /**
     * Outputs statements for dropping rules.
     *
     * @param writer           writer the output should be written to
     * @param oldSchema        original schema
     * @param newSchema        new schema
     * @param searchPathHelper search path helper
     */
    fun dropRules(
        writer: PrintWriter,
        oldSchema: PgSchema?, newSchema: PgSchema?,
        searchPathHelper: SearchPathHelper
    ) {
        for (newRelation in newSchema!!.rels) {
            val oldRelation: PgRelation?
            oldRelation = oldSchema?.getRelation(newRelation.name)

            // Drop rules that no more exist or are modified
            for (rule in dropRules(oldRelation, newRelation)) {
                searchPathHelper.outputSearchPath(writer)
                writer.println()
                writer.println(rule.dropSQL)
            }
        }
    }

    /**
     * Returns list of rules that should be dropped.
     *
     * @param oldRelation original relation
     * @param newRelation new relation
     *
     * @return list of rules that should be dropped
     */
    private fun dropRules(
        oldRelation: PgRelation?,
        newRelation: PgRelation?
    ): List<PgRule> {
        val list: MutableList<PgRule> = ArrayList()
        if (newRelation != null && oldRelation != null) {
            val newRules = newRelation.rules
            for (oldRule in oldRelation.rules) {
                if (!newRules!!.contains(oldRule)) {
                    list.add(oldRule)
                }
            }
        }
        return list
    }

    /**
     * Returns list of rules that should be added.
     *
     * @param oldRelation original relation
     * @param newRelation new relation
     *
     * @return list of rules that should be added
     */
    private fun getNewRules(
        oldRelation: PgRelation?,
        newRelation: PgRelation?
    ): List<PgRule> {
        val list: MutableList<PgRule> = ArrayList()
        if (newRelation != null) {
            if (oldRelation == null) {
                list.addAll(newRelation.rules)
            } else {
                for (newRule in newRelation.rules) {
                    if (!oldRelation.rules.contains(newRule)) {
                        list.add(newRule)
                    }
                }
            }
        }
        return list
    }
}