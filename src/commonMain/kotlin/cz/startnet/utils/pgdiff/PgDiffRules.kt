/**
 * Copyright 2018 StartNet s.r.o.
 *
 * Distributed under MIT license
 */
package cz.startnet.utils.pgdiff

import cz.startnet.utils.pgdiff.schema.PgRelation
import cz.startnet.utils.pgdiff.schema.PgRule
import cz.startnet.utils.pgdiff.schema.PgSchema
import kotlin.text.StringBuilder

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
     */
    fun createRules(
        writer: StringBuilder,
        oldSchema: PgSchema?, newSchema: PgSchema?
    ) {
        for (newRelation in newSchema?.rels.orEmpty()) {
            val oldRelation = oldSchema?.getRelation(newRelation.name)

            // Add new rules
            for (rule in getNewRules(oldRelation, newRelation)) {

                writer.println()
                writer.appendLine(rule.creationSQL)
            }
        }
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
        oldRelation: PgRelation<*, *>?,
        newRelation: PgRelation<*, *>?
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