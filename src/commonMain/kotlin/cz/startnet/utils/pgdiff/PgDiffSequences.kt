/**
 * Copyright 2006 StartNet s.r.o.
 *
 * Distributed under MIT license
 */
package cz.startnet.utils.pgdiff

import cz.startnet.utils.pgdiff.schema.PgSchema
import cz.startnet.utils.pgdiff.schema.PgSequence
import kotlin.text.StringBuilder

/**
 * Diffs sequences.
 *
 * @author fordfrog
 */
object PgDiffSequences {
    /**
     * Outputs statements for creation of new sequences.
     *
     * @param writer           writer the output should be written to
     * @param oldSchema        original schema
     * @param newSchema        new schema
     */
    fun createSequences(
        writer: StringBuilder,
        oldSchema: PgSchema?, newSchema: PgSchema
    ) {
        // Add new sequences
        for (sequence in newSchema.sequences) {
            if (oldSchema != null && oldSchema.sequences.containsSame(sequence)) {
                continue
            }
            writer.println()
            writer.appendLine(sequence.creationSQL)
            for (sequencePrivilege in sequence
                .privileges) {
                writer.appendLine(
                    "REVOKE ALL ON SEQUENCE "
                            + PgDiffUtils.getQuotedName(sequence.name)
                            + " FROM " + sequencePrivilege.roleName + ";"
                )
                if ("" != sequencePrivilege.getPrivilegesSQL(true)) {
                    writer.appendLine(
                        "GRANT "
                                + sequencePrivilege.getPrivilegesSQL(true)
                                + " ON SEQUENCE "
                                + PgDiffUtils.getQuotedName(sequence.name)
                                + " TO " + sequencePrivilege.roleName
                                + " WITH GRANT OPTION;"
                    )
                }
                if ("" != sequencePrivilege.getPrivilegesSQL(false)) {
                    writer.appendLine(
                        "GRANT "
                                + sequencePrivilege.getPrivilegesSQL(false)
                                + " ON SEQUENCE "
                                + PgDiffUtils.getQuotedName(sequence.name)
                                + " TO " + sequencePrivilege.roleName
                                + ";"
                    )
                }
            }
            sequence.owner.let {
                sequence.ownerSQL(writer)
            }
        }
    }

    /**
     * Outputs statements for altering of new sequences.
     *
     * @param writer           writer the output should be written to
     * @param oldSchema        original schema
     * @param newSchema        new schema
     */
    fun alterCreatedSequences(
        writer: StringBuilder,
        oldSchema: PgSchema?, newSchema: PgSchema?
    ) {
        // Alter created sequences
        for (sequence in newSchema!!.sequences) {
            if ((oldSchema == null
                        || !oldSchema.containsSequence(sequence.name))
                && !sequence.ownedBy.isNullOrEmpty()
            ) {

                writer.println()
                writer.appendLine(sequence.ownedBySQL)
            }
        }
    }

    /**
     * Outputs statement for modified sequences.
     *
     * @param writer           writer the output should be written to
     * @param oldSchema        original schema
     * @param newSchema        new schema
     */
    fun alterSequences(
        writer: StringBuilder,
        oldSchema: PgSchema?, newSchema: PgSchema
    ) {
        if (oldSchema == null) {
            return
        }
        val sbSQL = StringBuilder(100)
        for (newSequence in newSchema.sequences) {
            val oldSequence = oldSchema.sequences.getSame(newSequence) ?: continue
            sbSQL.setLength(0)
            val oldDataType = oldSequence.dataType
            val newDataType = newSequence.dataType
            if (newDataType != null
                && newDataType != oldDataType
            ) {
                sbSQL.append(System.getProperty("line.separator"))
                sbSQL.append("\tAS ")
                sbSQL.append(newDataType)
            }
            val oldIncrement = oldSequence.increment
            val newIncrement = newSequence.increment
            if (newIncrement != null
                && newIncrement != oldIncrement
            ) {
                sbSQL.append(System.getProperty("line.separator"))
                sbSQL.append("\tINCREMENT BY ")
                sbSQL.append(newIncrement)
            }
            val oldMinValue = oldSequence.minValue
            val newMinValue = newSequence.minValue
            if (newMinValue == null && oldMinValue != null) {
                sbSQL.append(System.getProperty("line.separator"))
                sbSQL.append("\tNO MINVALUE")
            } else if (newMinValue != null
                && newMinValue != oldMinValue
            ) {
                sbSQL.append(System.getProperty("line.separator"))
                sbSQL.append("\tMINVALUE ")
                sbSQL.append(newMinValue)
            }
            val oldMaxValue = oldSequence.maxValue
            val newMaxValue = newSequence.maxValue
            if (newMaxValue == null && oldMaxValue != null) {
                sbSQL.append(System.getProperty("line.separator"))
                sbSQL.append("\tNO MAXVALUE")
            } else if (newMaxValue != null
                && newMaxValue != oldMaxValue
            ) {
                sbSQL.append(System.getProperty("line.separator"))
                sbSQL.append("\tMAXVALUE ")
                sbSQL.append(newMaxValue)
            }
            val oldStart = oldSequence.startWith
            val newStart = newSequence.startWith
            if (newStart != null && newStart != oldStart) {
                sbSQL.append(System.getProperty("line.separator"))
                sbSQL.append("\tSTART WITH ")
                sbSQL.append(newStart)
            }
            val oldCache = oldSequence.cache
            val newCache = newSequence.cache
            if (newCache != null && newCache != oldCache) {
                sbSQL.append(System.getProperty("line.separator"))
                sbSQL.append("\tCACHE ")
                sbSQL.append(newCache)
            }
            val oldCycle = oldSequence.isCycle
            val newCycle = newSequence.isCycle
            if (oldCycle && !newCycle) {
                sbSQL.append(System.getProperty("line.separator"))
                sbSQL.append("\tNO CYCLE")
            } else if (!oldCycle && newCycle) {
                sbSQL.append(System.getProperty("line.separator"))
                sbSQL.append("\tCYCLE")
            }
            val oldOwnedBy = oldSequence.ownedBy
            val newOwnedBy = newSequence.ownedBy
            if (newOwnedBy != null && newOwnedBy != oldOwnedBy) {
                sbSQL.append(System.getProperty("line.separator"))
                sbSQL.append("\tOWNED BY ")
                sbSQL.append(newOwnedBy)
            }
            if (sbSQL.length > 0) {
                writer.println()
                writer.append(
                    "ALTER SEQUENCE "
                            + PgDiffUtils.getQuotedName(newSequence.name)
                )
                writer.append(sbSQL.toString())
                writer.println(';')
            }
            if (oldSequence.comment == null
                && newSequence.comment != null
                || oldSequence.comment != null && newSequence.comment != null && oldSequence.comment != newSequence.comment
            ) {
                writer.println()
                writer.append("COMMENT ON SEQUENCE ")
                writer.append(PgDiffUtils.getQuotedName(newSequence.name))
                writer.append(" IS ")
                writer.print(newSequence.comment)
                writer.println(';')
            } else if (oldSequence.comment != null
                && newSequence.comment == null
            ) {
                writer.println()
                writer.append("COMMENT ON SEQUENCE ")
                writer.append(newSequence.name)
                writer.appendLine(" IS NULL;")
            }
            alterPrivileges(writer, oldSequence, newSequence)

            if (oldSequence.owner != newSequence.owner) {
                newSequence.ownerSQL(writer)
            }
        }
    }

    private fun alterPrivileges(
        writer: StringBuilder,
        oldSequence: PgSequence, newSequence: PgSequence?
    ) {
        val emptyLinePrinted = false
        for (oldSequencePrivilege in oldSequence
            .privileges) {
            val newSequencePrivilege = newSequence?.getPrivilege(oldSequencePrivilege.roleName)
            if (newSequencePrivilege == null) {
                if (!emptyLinePrinted) {
                    writer.println()
                }
                writer.appendLine(
                    "REVOKE ALL ON SEQUENCE "
                            + PgDiffUtils.getQuotedName(oldSequence.name)
                            + " FROM " + oldSequencePrivilege.roleName + ";"
                )
            } else if (!oldSequencePrivilege.isSimilar(newSequencePrivilege)) {
                if (!emptyLinePrinted) {
                    writer.println()
                }
                writer.appendLine(
                    "REVOKE ALL ON SEQUENCE "
                            + PgDiffUtils.getQuotedName(newSequence.name)
                            + " FROM " + newSequencePrivilege.roleName + ";"
                )
                if ("" != newSequencePrivilege.getPrivilegesSQL(true)) {
                    writer.appendLine(
                        "GRANT "
                                + newSequencePrivilege.getPrivilegesSQL(true)
                                + " ON SEQUENCE "
                                + PgDiffUtils.getQuotedName(newSequence.name)
                                + " TO " + newSequencePrivilege.roleName
                                + " WITH GRANT OPTION;"
                    )
                }
                if ("" != newSequencePrivilege.getPrivilegesSQL(false)) {
                    writer.appendLine(
                        "GRANT "
                                + newSequencePrivilege.getPrivilegesSQL(false)
                                + " ON SEQUENCE "
                                + PgDiffUtils.getQuotedName(newSequence.name)
                                + " TO " + newSequencePrivilege.roleName + ";"
                    )
                }
            } // else similar privilege will not be updated
        }
        for (newSequencePrivilege in newSequence?.privileges.orEmpty()) {
            val oldSequencePrivilege = oldSequence
                .getPrivilege(newSequencePrivilege.roleName)
            if (oldSequencePrivilege == null) {
                if (!emptyLinePrinted) {
                    writer.println()
                }
                writer.appendLine(
                    "REVOKE ALL ON SEQUENCE "
                            + PgDiffUtils.getQuotedName(newSequence!!.name)
                            + " FROM " + newSequencePrivilege.roleName + ";"
                )
                if ("" != newSequencePrivilege.getPrivilegesSQL(true)) {
                    writer.appendLine(
                        "GRANT "
                                + newSequencePrivilege.getPrivilegesSQL(true)
                                + " ON SEQUENCE "
                                + PgDiffUtils.getQuotedName(newSequence.name)
                                + " TO " + newSequencePrivilege.roleName
                                + " WITH GRANT OPTION;"
                    )
                }
                if ("" != newSequencePrivilege.getPrivilegesSQL(false)) {
                    writer.appendLine(
                        "GRANT "
                                + newSequencePrivilege.getPrivilegesSQL(false)
                                + " ON SEQUENCE "
                                + PgDiffUtils.getQuotedName(newSequence.name)
                                + " TO " + newSequencePrivilege.roleName + ";"
                    )
                }
            }
        }
    }
}