/**
 * Copyright 2018 StartNet s.r.o.
 *
 * Distributed under MIT license
 */
package cz.startnet.utils.pgdiff

import cz.startnet.utils.pgdiff.schema.PgSchema
import kotlin.text.StringBuilder

/**
 * Diffs rules.
 *
 * @author jalissonmello
 */
object PgDiffGrant {
    /**
     * Outputs statements for creation of new triggers.
     *
     * @param writer           writer the output should be written to
     * @param oldSchema        original schema
     * @param newSchema        new schema
     */
    fun createGrants(writer: StringBuilder, oldSchema: PgSchema?, newSchema: PgSchema?) {
        val oldGrants: List<String?>?
        oldGrants = oldSchema?.grants
        for (newGrant in newSchema?.grants.orEmpty()) {
            if (oldGrants != null && oldGrants.contains(newGrant)) {
                continue
            }
            writer.println()
            writer.println(newGrant)
        }
    }
}