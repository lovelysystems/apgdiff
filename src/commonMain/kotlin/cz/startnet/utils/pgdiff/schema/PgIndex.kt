/**
 * Copyright 2006 StartNet s.r.o.
 *
 * Distributed under MIT license
 */
package cz.startnet.utils.pgdiff.schema

import cz.startnet.utils.pgdiff.PgDiffUtils

/**
 * Stores table index information.
 *
 * @author fordfrog
 */
class PgIndex
/**
 * Creates a new PgIndex object.
 *
 * @param name [.name]
 */(
    /**
     * Name of the index.
     */
    val name: String
) {
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
     * Definition of the index.
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
     * Getter for [.only].
     *
     * @return [.only]
     */
    /**
     * Setter for [.only].
     *
     * @param only [.only]
     */
    /**
     * Indicates not to recurse creating indexes on partitions, if the table is partitioned.
     */
    var only = false
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
     * Table name the index is defined on.
     */
    var tableName: String? = null
    /**
     * Getter for [.unique].
     *
     * @return [.unique]
     */
    /**
     * Setter for [.unique].
     *
     * @param unique [.unique]
     */
    /**
     * Whether the index is unique.
     */
    var isUnique = false
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
     * Creates and returns SQL for creation of the index.
     *
     * @return created SQL
     */
    val creationSQL: String
        get() {
            val sbSQL = StringBuilder(100)
            sbSQL.append("CREATE ")
            if (isUnique) {
                sbSQL.append("UNIQUE ")
            }
            sbSQL.append("INDEX ")
            sbSQL.append(PgDiffUtils.createIfNotExists)
            sbSQL.append(PgDiffUtils.getQuotedName(name))
            sbSQL.append(" ON ")
            if (only) {
                sbSQL.append("ONLY ")
            }
            sbSQL.append(PgDiffUtils.getQuotedName(tableName))
            sbSQL.append(' ')
            sbSQL.append(definition)
            sbSQL.append(';')
            if (comment != null && !comment!!.isEmpty()) {
                sbSQL.appendLine()
                sbSQL.appendLine()
                sbSQL.append("COMMENT ON INDEX ")
                sbSQL.append(PgDiffUtils.getQuotedName(name))
                sbSQL.append(" IS ")
                sbSQL.append(comment)
                sbSQL.append(';')
            }
            return sbSQL.toString()
        }

    /**
     * Creates and returns SQL statement for dropping the index.
     *
     * @return created SQL statement
     */
    val dropSQL: String
        get() = "DROP INDEX " + PgDiffUtils.dropIfExists + PgDiffUtils.getQuotedName(name) + ";"

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
        } else if (`object` is PgIndex) {
            val index = `object`
            equals =
                definition == index.definition && name == index.name && tableName == index.tableName && isUnique == index.isUnique
        }
        return equals
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    override fun hashCode(): Int {
        return (javaClass.name + "|" + definition + "|" + name + "|"
                + tableName + "|" + isUnique).hashCode()
    }
}