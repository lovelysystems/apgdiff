/**
 * Copyright 2006 StartNet s.r.o.
 *
 * Distributed under MIT license
 */
package cz.startnet.utils.pgdiff.schema

import cz.startnet.utils.pgdiff.PgDiffUtils

class PgRule(
    name: String,
    val relationName: QualifiedName,
    val event: String,
    val query: String?, position: Int,
) : DBObject("RULE", name, position) {


    /**
     * Creates and returns SQL for creation of the view.
     *
     * @return created SQL statement
     */
    val creationSQL: String
        get() {
            val sbSQL = StringBuilder(query!!.length * 2)
            sbSQL.append("CREATE ")
            sbSQL.append(objectType)
            sbSQL.append(' ')
            sbSQL.append(PgDiffUtils.getQuotedName(name))
            sbSQL.append(" AS")
            sbSQL.appendLine()
            sbSQL.append(" ON ")
            sbSQL.append(event)
            sbSQL.append(" TO ")
            sbSQL.append(relationName)
            sbSQL.appendLine()
            sbSQL.append(" ")
            sbSQL.append(query)
            sbSQL.append(";")
            if (comment != null) {
                sbSQL.appendLine()
                sbSQL.append(commentSQL)
            }
            return sbSQL.toString()
        }


    override fun equals(other: Any?): Boolean {
        var equals = false
        if (this === other) {
            equals = true
        } else if (other is PgRule) {
            val rule = other
            equals =
                event === rule.event && relationName == rule.relationName && name == rule.name && query == rule.query
        }
        return equals
    }

    override val commentSQL: String
        get() = "COMMENT ON RULE ${quotedIdentifier()} ON ${relationName} IS $comment;"

}