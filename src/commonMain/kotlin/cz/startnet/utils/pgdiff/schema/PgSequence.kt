/**
 * Copyright 2006 StartNet s.r.o.
 *
 * Distributed under MIT license
 */
package cz.startnet.utils.pgdiff.schema

import cz.startnet.utils.pgdiff.PgDiffUtils

class PgSequence(name: String, position: Int) : DBObject("SEQUENCE", name, position) {
    /**
     * Value for CACHE or null if no value is specified.
     */
    var cache: String? = null

    /**
     * Value for INCREMENT BY or null if no value is specified.
     */
    var increment: String? = null

    /**
     * Value for MAXVALUE or null if no value is specified.
     */
    var maxValue: String? = null

    /**
     * Value for MINVALUE or null if no value is specified.
     */
    var minValue: String? = null

    /**
     * Value for START WITH or null if no value is specified.
     */
    var startWith: String? = null

    /**
     * True if CYCLE, false if NO CYCLE.
     */
    var isCycle = false

    /**
     * Column the sequence is owned by.
     */
    var ownedBy: String? = null

    /**
     * List of privileges defined on the sequence.
     */
    val privileges: MutableList<PgSequencePrivilege> = ArrayList()

    /**
     * Value for AS or null if no value is specified.
     */
    var dataType: String? = null

    /**
     * Creates and returns SQL statement for creation of the sequence.
     *
     * @return created SQL statement
     */
    val creationSQL: String
        get() {
            val sbSQL = StringBuilder(100)
            sbSQL.append("CREATE SEQUENCE ")
            sbSQL.append(PgDiffUtils.createIfNotExists)
            sbSQL.append(PgDiffUtils.getQuotedName(name))
            if (dataType != null) {
                sbSQL.appendLine()
                sbSQL.append("\tAS ")
                sbSQL.append(dataType)
            }
            if (startWith != null) {
                sbSQL.appendLine()
                sbSQL.append("\tSTART WITH ")
                sbSQL.append(startWith)
            }
            if (increment != null) {
                sbSQL.appendLine()
                sbSQL.append("\tINCREMENT BY ")
                sbSQL.append(increment)
            }
            sbSQL.appendLine()
            sbSQL.append("\t")
            if (maxValue == null) {
                sbSQL.append("NO MAXVALUE")
            } else {
                sbSQL.append("MAXVALUE ")
                sbSQL.append(maxValue)
            }
            sbSQL.appendLine()
            sbSQL.append("\t")
            if (minValue == null) {
                sbSQL.append("NO MINVALUE")
            } else {
                sbSQL.append("MINVALUE ")
                sbSQL.append(minValue)
            }
            if (cache != null) {
                sbSQL.appendLine()
                sbSQL.append("\tCACHE ")
                sbSQL.append(cache)
            }
            if (isCycle) {
                sbSQL.appendLine()
                sbSQL.append("\tCYCLE")
            }
            sbSQL.append(';')
            if (comment != null && !comment!!.isEmpty()) {
                sbSQL.appendLine()
                sbSQL.appendLine()
                sbSQL.append("COMMENT ON SEQUENCE ")
                sbSQL.append(PgDiffUtils.getQuotedName(name))
                sbSQL.append(" IS ")
                sbSQL.append(comment)
                sbSQL.append(';')
            }
            return sbSQL.toString()
        }

    /**
     * Creates and returns SQL statement for modification "OWNED BY" parameter.
     *
     * @return created SQL statement
     */
    val ownedBySQL: String
        get() {
            val sbSQL = StringBuilder(100)
            sbSQL.append("ALTER SEQUENCE ")
            sbSQL.append(PgDiffUtils.getQuotedName(name))
            if (ownedBy != null && !ownedBy!!.isEmpty()) {
                sbSQL.appendLine()
                sbSQL.append("\tOWNED BY ")
                sbSQL.append(ownedBy)
            }
            sbSQL.append(';')
            return sbSQL.toString()
        }

    fun getPrivilege(roleName: String?): PgSequencePrivilege? {
        for (privilege in privileges) {
            if (privilege.roleName == roleName) {
                return privilege
            }
        }
        return null
    }

    fun addPrivilege(privilege: PgSequencePrivilege) {
        privileges.add(privilege)
    }
}