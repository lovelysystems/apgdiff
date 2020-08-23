/**
 * Copyright 2006 StartNet s.r.o.
 *
 * Distributed under MIT license
 */
package cz.startnet.utils.pgdiff.schema

import cz.startnet.utils.pgdiff.PgDiffUtils
import cz.startnet.utils.pgdiff.schema.PgTrigger.EventTimeQualification
import java.util.*

/**
 * Stores trigger information.
 *
 * @author fordfrog
 */
class PgTrigger {
    /**
     * Enumeration of when, with respect to event, a trigger should fire.
     * e.g. BEFORE, AFTER or INSTEAD OF an event.
     */
    enum class EventTimeQualification {
        before, after, instead_of;

        companion object {
            private var stringRepresentation: Map<EventTimeQualification, String>? = null
            fun toString(eventTimeQualification: EventTimeQualification): String? {
                return stringRepresentation!![eventTimeQualification]
            }

            init {
                val aMap = HashMap<EventTimeQualification, String>()
                aMap.put(before, "BEFORE")
                aMap.put(after, "AFTER")
                aMap.put(instead_of, "INSTEAD OF")
                stringRepresentation = Collections.unmodifiableMap(aMap)
            }
        }
    }
    /**
     * Getter for [.function].
     *
     * @return [.function]
     */
    /**
     * Setter for [.function].
     *
     * @param function [.function]
     */
    /**
     * Function name and arguments that should be fired on the trigger.
     */
    var function: String? = null
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
     * Name of the trigger.
     */
    var name: String? = null
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
     * Name of the relation the trigger is defined on.
     */
    var relationName: String? = null
    /**
     * Getter for [.eventTimeQualification].
     *
     * @return [.eventTimeQualification]
     */
    /**
     * Setter for [.eventTimeQualification].
     *
     * @param eventTimeQualification [.eventTimeQualification]
     */
    /**
     * Whether the trigger should be fired BEFORE, AFTER or INSTEAD OF an event.
     * Default is before.
     */
    var eventTimeQualification = EventTimeQualification.before
    /**
     * Getter for [.forEachRow].
     *
     * @return [.forEachRow]
     */
    /**
     * Setter for [.forEachRow].
     *
     * @param forEachRow [.forEachRow]
     */
    /**
     * Whether the trigger should be fired FOR EACH ROW or FOR EACH STATEMENT.
     * Default is FOR EACH STATEMENT.
     */
    var isForEachRow = false
    /**
     * Getter for [.onDelete].
     *
     * @return [.onDelete]
     */
    /**
     * Setter for [.onDelete].
     *
     * @param onDelete [.onDelete]
     */
    /**
     * Whether the trigger should be fired on DELETE.
     */
    var isOnDelete = false
    /**
     * Getter for [.onInsert].
     *
     * @return [.onInsert]
     */
    /**
     * Setter for [.onInsert].
     *
     * @param onInsert [.onInsert]
     */
    /**
     * Whether the trigger should be fired on INSERT.
     */
    var isOnInsert = false
    /**
     * Getter for [.onUpdate].
     *
     * @return [.onUpdate]
     */
    /**
     * Setter for [.onUpdate].
     *
     * @param onUpdate [.onUpdate]
     */
    /**
     * Whether the trigger should be fired on UPDATE.
     */
    var isOnUpdate = false
    /**
     * Getter for [.onTruncate].
     *
     * @return [.onTruncate]
     */
    /**
     * Setter for [.onTruncate].
     *
     * @param onTruncate [.onTruncate]
     */
    /**
     * Whether the trigger should be fired on TRUNCATE.
     */
    var isOnTruncate = false

    /**
     * Optional list of columns for UPDATE event.
     */
    private val updateColumns: MutableList<String> = ArrayList()
    /**
     * Getter for [.when].
     *
     * @return [.when]
     */
    /**
     * Setter for [.when].
     *
     * @param when [.when]
     */
    /**
     * WHEN condition.
     */
    var `when`: String? = null
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
     * Getter for [.referencing].
     *
     * @return [.referencing]
     */
    /**
     * Setter for [.referencing].
     *
     * @param referencing [.referencing]
     */
    /**
     * Referencing clause.
     */
    var referencing: String? = null
    /**
     * Getter for [.disable].
     *
     * @return [.disable]
     */
    /**
     * Setter for [.disable].
     *
     * @param disable [.disable]
     */
    /**
     * Disable.
     */
    var isDisable = false

    /**
     * Creates and returns SQL for creation of trigger.
     *
     * @return created SQL
     */
    val creationSQL: String
        get() {
            val sbSQL = StringBuilder(100)
            sbSQL.append("CREATE TRIGGER ")
            sbSQL.append(PgDiffUtils.getQuotedName(name))
            sbSQL.append(System.getProperty("line.separator"))
            sbSQL.append("\t")
            sbSQL.append(EventTimeQualification.toString(eventTimeQualification))
            var firstEvent = true
            if (isOnInsert) {
                sbSQL.append(" INSERT")
                firstEvent = false
            }
            if (isOnUpdate) {
                if (firstEvent) {
                    firstEvent = false
                } else {
                    sbSQL.append(" OR")
                }
                sbSQL.append(" UPDATE")
                if (!updateColumns.isEmpty()) {
                    sbSQL.append(" OF")
                    var first = true
                    for (columnName in updateColumns) {
                        if (first) {
                            first = false
                        } else {
                            sbSQL.append(',')
                        }
                        sbSQL.append(' ')
                        sbSQL.append(columnName)
                    }
                }
            }
            if (isOnDelete) {
                if (!firstEvent) {
                    sbSQL.append(" OR")
                }
                sbSQL.append(" DELETE")
            }
            if (isOnTruncate) {
                if (!firstEvent) {
                    sbSQL.append(" OR")
                }
                sbSQL.append(" TRUNCATE")
            }
            sbSQL.append(" ON ")
            sbSQL.append(PgDiffUtils.getQuotedName(relationName))
            sbSQL.append(System.getProperty("line.separator"))
            if (referencing != null && !referencing!!.isEmpty()) {
                sbSQL.append(referencing)
                sbSQL.append(System.getProperty("line.separator"))
            }
            sbSQL.append("\tFOR EACH ")
            sbSQL.append(if (isForEachRow) "ROW" else "STATEMENT")
            if (`when` != null && !`when`!!.isEmpty()) {
                sbSQL.append(System.getProperty("line.separator"))
                sbSQL.append("\tWHEN (")
                sbSQL.append(`when`)
                sbSQL.append(')')
            }
            sbSQL.append(System.getProperty("line.separator"))
            sbSQL.append("\tEXECUTE PROCEDURE ")
            sbSQL.append(function)
            sbSQL.append(';')
            if (comment != null && !comment!!.isEmpty()) {
                sbSQL.append(System.getProperty("line.separator"))
                sbSQL.append(System.getProperty("line.separator"))
                sbSQL.append("COMMENT ON TRIGGER ")
                sbSQL.append(PgDiffUtils.getQuotedName(name))
                sbSQL.append(" ON ")
                sbSQL.append(PgDiffUtils.getQuotedName(relationName))
                sbSQL.append(" IS ")
                sbSQL.append(comment)
                sbSQL.append(';')
            }
            return sbSQL.toString()
        }

    /**
     * Creates and returns SQL for dropping the trigger.
     *
     * @return created SQL
     */
    val dropSQL: String
        get() = ("DROP TRIGGER " + PgDiffUtils.dropIfExists + PgDiffUtils.getQuotedName(name) + " ON "
                + PgDiffUtils.getQuotedName(relationName) + ";")

//    /**
//     * Getter for [.updateColumns].
//     *
//     * @return [.updateColumns]
//     */
//    fun getUpdateColumns(): List<String> {
//        return Collections.unmodifiableList(updateColumns)
//    }

    /**
     * Adds column name to the list of update columns.
     *
     * @param columnName column name
     */
    fun addUpdateColumn(columnName: String) {
        updateColumns.add(columnName)
    }

    override fun equals(`object`: Any?): Boolean {
        var equals = false
        if (this === `object`) {
            equals = true
        } else if (`object` is PgTrigger) {
            val trigger = `object`
            equals = (eventTimeQualification == trigger.eventTimeQualification
                    && isForEachRow == trigger.isForEachRow
                    && function == trigger.function && name == trigger.name && isOnDelete == trigger.isOnDelete
                    && isOnInsert == trigger.isOnInsert
                    && isOnUpdate == trigger.isOnUpdate
                    && isOnTruncate == trigger.isOnTruncate
                    && relationName == trigger.relationName)
            if (equals) {
                val sorted1: List<String> = updateColumns.sorted()
                val sorted2: List<String?> = trigger.updateColumns.sorted()
                equals = sorted1 == sorted2
            }
        }
        return equals
    }

    override fun hashCode(): Int {
        return (javaClass.name + "|" + eventTimeQualification + "|" + isForEachRow + "|"
                + function + "|" + name + "|" + isOnDelete + "|" + isOnInsert + "|"
                + isOnUpdate + "|" + isOnTruncate + "|" + relationName
                + "|" + isDisable).hashCode()
    }

    /**
     * Creates and returns SQL for creation of trigger.
     *
     * @return created SQL
     */
    val disableOrEnableSQL: String
        get() {
            val sbSQL = StringBuilder(100)
            sbSQL.append("ALTER TABLE ")
            sbSQL.append(relationName)
            if (isDisable) {
                sbSQL.append(" DISABLE")
            } else {
                sbSQL.append(" ENABLE")
            }
            sbSQL.append(" TRIGGER ")
            sbSQL.append(name)
            sbSQL.append(';')
            sbSQL.append(System.getProperty("line.separator"))
            return sbSQL.toString()
        }
}