/**
 * Copyright 2006 StartNet s.r.o.
 *
 * Distributed under MIT license
 */
package cz.startnet.utils.pgdiff.schema

import cz.startnet.utils.pgdiff.PgDiffUtils

/**
 * Stores table constraint information.
 *
 * @author fordfrog
 */
class PgConstraint(var name: String) {
    /**
     * Getter for [.definition].
     *
     * @return [.definition]
     */
    /**
     * Setter for [.definition].
     *
     * @param definition [.definition]
     */
    /**
     * Definition of the constraint.
     */
    var definition: String? = null
    /**
     * Getter for [.name].
     *
     * @return [.name]
     */
    /**
     * Setter for [.name].
     *
     * @param name [.name]
     */
    /**
     * Getter for [.tableName].
     *
     * @return [.tableName]
     */
    /**
     * Setter for [.tableName].
     *
     * @param tableName [.tableName]
     */
    /**
     * Name of the table the constraint is defined on.
     */
    var tableName: String? = null
    /**
     * Getter for [.comment].
     *
     * @return [.comment]
     */
    /**
     * Setter for [.comment].
     *
     * @param comment [.comment]
     */
    /**
     * Comment.
     */
    var comment: String? = null

    /**
     * Creates and returns SQL for creation of the constraint.
     * @return created SQL
     */
    val creationSQL: String
        get() {
            val sbSQL = StringBuilder(100)
            sbSQL.append("ALTER TABLE ")
            sbSQL.append(PgDiffUtils.getQuotedName(tableName))
            sbSQL.appendLine()
            sbSQL.append("\tADD CONSTRAINT ")
            sbSQL.append(PgDiffUtils.createIfNotExists)
            sbSQL.append(PgDiffUtils.getQuotedName(name))
            sbSQL.append(' ')
            sbSQL.append(definition)
            sbSQL.append(';')
            if (comment != null && comment!!.isNotEmpty()) {
                sbSQL.appendLine()
                sbSQL.appendLine()
                sbSQL.append("COMMENT ON CONSTRAINT ")
                sbSQL.append(PgDiffUtils.getQuotedName(name))
                sbSQL.append(" ON ")
                sbSQL.append(PgDiffUtils.getQuotedName(tableName))
                sbSQL.append(" IS ")
                sbSQL.append(comment)
                sbSQL.append(';')
            }
            return sbSQL.toString()
        }

    /**
     * Creates and returns SQL for dropping the constraint.
     * @return created SQL
     */
    val dropSQL: String
        get() {
            val sbSQL = StringBuilder(100)
            sbSQL.append("ALTER TABLE ")
            sbSQL.append(PgDiffUtils.getQuotedName(tableName))
            sbSQL.appendLine()
            sbSQL.append("\tDROP CONSTRAINT ")
            sbSQL.append(PgDiffUtils.dropIfExists)
            sbSQL.append(PgDiffUtils.getQuotedName(name))
            sbSQL.append(';')
            return sbSQL.toString()
        }

    /**
     * Returns true if this is a PRIMARY KEY constraint, otherwise false.
     *
     * @return true if this is a PRIMARY KEY constraint, otherwise false
     */
    val isPrimaryKeyConstraint: Boolean
        get() = PATTERN_PRIMARY_KEY.matches(definition!!)

    /**
     * {@inheritDoc}
     *
     * @param object {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    override fun equals(`object`: Any?): Boolean {
        var equals = false
        if (this === `object`) {
            equals = true
        } else if (`object` is PgConstraint) {
            equals = definition == `object`.definition && name == `object`.name && tableName == `object`.tableName
        }
        return equals
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    override fun hashCode(): Int {
        return (this::class.qualifiedName + "|" + definition + "|" + name + "|"
                + tableName).hashCode()
    }

    companion object {
        /**
         * Pattern for checking whether the constraint is PRIMARY KEY constraint.
         */
        private val PATTERN_PRIMARY_KEY = Regex(".*PRIMARY[\\s]+KEY.*", RegexOption.IGNORE_CASE)
    }
}
