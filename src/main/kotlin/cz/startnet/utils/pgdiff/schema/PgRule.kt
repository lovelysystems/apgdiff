/**
 * Copyright 2006 StartNet s.r.o.
 *
 * Distributed under MIT license
 */
package cz.startnet.utils.pgdiff.schema

import cz.startnet.utils.pgdiff.PgDiffUtils

/**
 * Stores rule information.
 *
 * @author jalissonmello
 */
class PgRule(name: String) : DBObject(name, "RULE") {

    var query: String? = null

    /**
     * Name of the relation the rule is defined on.
     */
    var relationName: String? = null

    /**
     * event of rule.
     */
    var event: String? = null

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
            sbSQL.append(System.getProperty("line.separator"))
            sbSQL.append(" ON ")
            sbSQL.append(event)
            sbSQL.append(" TO ")
            sbSQL.append(relationName)
            sbSQL.append(System.getProperty("line.separator"))
            sbSQL.append(" ")
            sbSQL.append(query)
            sbSQL.append(";")
            sbSQL.append(commentSQL)
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
}