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
class PgRule(name: String) : PgRelation() {
    /**
     * Getter for [.query].
     *
     * @return [.query]
     */
    /**
     * Setter for [.query].
     *
     * @param query [.query]
     */
    var query: String? = null
    /**
     * Getter for [.relationName].
     *
     * @return [.relationName]
     */
    /**
     * Setter for [.relationName].
     *
     * @param relationName [.relationName]
     */
    /**
     * Name of the relation the rule is defined on.
     */
    var relationName: String? = null
    /**
     * Getter for [.event].
     *
     * @return [.event]
     */
    /**
     * Setter for [.event].
     *
     * @param event [.event]
     */
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
            sbSQL.append(relationKind)
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
            sbSQL.append(commentDefinitionSQL)
            return sbSQL.toString()
        }
    override val relationKind: String
        get() = "RULE"

    /**
     * Creates and returns SQL for dropping the rule.
     *
     * @return created SQL
     */
    override val dropSQL: String
        get() = ("DROP RULE " + PgDiffUtils.dropIfExists + PgDiffUtils.getQuotedName(name) + " ON "
                + PgDiffUtils.getQuotedName(relationName) + ";")

    override fun equals(`object`: Any?): Boolean {
        var equals = false
        if (this === `object`) {
            equals = true
        } else if (`object` is PgRule) {
            val rule = `object`
            equals =
                event === rule.event && relationName == rule.relationName && name == rule.name && query == rule.query
        }
        return equals
    }

    /**
     * Creates a new PgView object.
     *
     * @param name [.name]
     */
    init {
        this.name = name
    }
}