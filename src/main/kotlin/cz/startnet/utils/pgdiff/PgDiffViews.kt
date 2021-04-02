/**
 * Copyright 2006 StartNet s.r.o.
 *
 * Distributed under MIT license
 */
package cz.startnet.utils.pgdiff

import cz.startnet.utils.pgdiff.schema.PgColumnPrivilege
import cz.startnet.utils.pgdiff.schema.PgSchema
import cz.startnet.utils.pgdiff.schema.PgView
import java.io.PrintWriter
import java.util.*

/**
 * Diffs views.
 *
 * @author fordfrog
 */
object PgDiffViews {
    /**
     * Outputs statements for creation of views.
     *
     * @param writer           writer the output should be written to
     * @param oldSchema        original schema
     * @param newSchema        new schema
     * @param searchPathHelper search path helper
     */
    fun createViews(
        writer: PrintWriter,
        oldSchema: PgSchema?, newSchema: PgSchema,
        searchPathHelper: SearchPathHelper
    ) {
        for (newView in newSchema.views) {
            val oldView = oldSchema?.getView(newView.name)
            if (oldSchema == null || !oldSchema.containsView(newView.name)
                || isViewModified(oldView, newView)
            ) {
                searchPathHelper.outputSearchPath(writer)
                writer.println()
                writer.println(newView.creationSQL)
                if (newView.ownerTo != null && oldView == null) {
                    writer.println()
                    writer.println(
                        "ALTER ${newView.relationKind} "
                                + PgDiffUtils.getQuotedName(newView.name)
                                + " OWNER TO " + newView.ownerTo + ";"
                    )
                }
                for (viewPrivilege in newView.privileges) {
                    writer.println(
                        "REVOKE ALL ON TABLE "
                                + PgDiffUtils.getQuotedName(newView.name)
                                + " FROM " + viewPrivilege.roleName + ";"
                    )
                    if ("" != viewPrivilege.getPrivilegesSQL(true)) {
                        writer.println(
                            "GRANT "
                                    + viewPrivilege.getPrivilegesSQL(true)
                                    + " ON TABLE "
                                    + PgDiffUtils.getQuotedName(newView.name)
                                    + " TO " + viewPrivilege.roleName
                                    + " WITH GRANT OPTION;"
                        )
                    }
                    if ("" != viewPrivilege.getPrivilegesSQL(false)) {
                        writer.println(
                            "GRANT "
                                    + viewPrivilege.getPrivilegesSQL(false)
                                    + " ON TABLE "
                                    + PgDiffUtils.getQuotedName(newView.name)
                                    + " TO " + viewPrivilege.roleName + ";"
                        )
                    }
                }
            }
        }
    }

    /**
     * Outputs statements for dropping views.
     *
     * @param writer           writer the output should be written to
     * @param oldSchema        original schema
     * @param newSchema        new schema
     * @param searchPathHelper search path helper
     */
    fun dropViews(
        writer: PrintWriter,
        oldSchema: PgSchema?, newSchema: PgSchema?,
        searchPathHelper: SearchPathHelper
    ) {
        if (oldSchema == null) {
            return
        }
        for (oldView in oldSchema.views) {
            val newView = newSchema!!.getView(oldView.name)
            if (newView == null || isViewModified(oldView, newView)) {
                searchPathHelper.outputSearchPath(writer)
                writer.println()
                writer.println(oldView.dropSQL)
            }
        }
    }

    /**
     * Returns true if either column names or query of the view has been
     * modified.
     *
     * @param oldView old view
     * @param newView new view
     *
     * @return true if view has been modified, otherwise false
     */
    private fun isViewModified(
        oldView: PgView?,
        newView: PgView?
    ): Boolean {
        if (oldView!!.query.trim { it <= ' ' } != newView!!.query.trim { it <= ' ' }) return true
        if (oldView.isMaterialized != newView.isMaterialized) return true
        val oldViewColumnNames = oldView.declaredColumnNames
        val newViewColumnNames = newView.declaredColumnNames
        return if (oldViewColumnNames != null && newViewColumnNames != null) {
            oldViewColumnNames != newViewColumnNames
        } else {
            // At least one of the two is null. Are both?
            oldViewColumnNames !== newViewColumnNames
        }
    }

    /**
     * Outputs statements for altering view default values.
     *
     * @param writer           writer
     * @param oldSchema        old schema
     * @param newSchema        new schema
     * @param searchPathHelper search path helper
     */
    fun alterViews(
        writer: PrintWriter,
        oldSchema: PgSchema?, newSchema: PgSchema?,
        searchPathHelper: SearchPathHelper
    ) {
        if (oldSchema == null) {
            return
        }
        for (oldView in oldSchema.views) {
            val newView = newSchema!!.getView(oldView.name) ?: continue
            diffDefaultValues(writer, oldView, newView, searchPathHelper)
            if (oldView.comment == null
                && newView.comment != null
                || oldView.comment != null && newView.comment != null && oldView.comment != newView.comment
            ) {
                searchPathHelper.outputSearchPath(writer)
                writer.println()
                writer.print("COMMENT ON ${newView.relationKind} ")
                writer.print(
                    PgDiffUtils.getQuotedName(newView.name)
                )
                writer.print(" IS ")
                writer.print(newView.comment)
                writer.println(';')
            } else if (oldView.comment != null
                && newView.comment == null
            ) {
                searchPathHelper.outputSearchPath(writer)
                writer.println()
                writer.print("COMMENT ON ${newView.relationKind} ")
                writer.print(PgDiffUtils.getQuotedName(newView.name))
                writer.println(" IS NULL;")
            }
            val columnNames: MutableList<String> = ArrayList(newView.columns.size)
            for (col in newView.columns) {
                columnNames.add(col.name)
            }
            for (col in oldView.columns) {
                if (!columnNames.contains(col.name)) {
                    columnNames.add(col.name)
                }
            }
            for (columnName in columnNames) {
                var oldComment: String? = null
                var newComment: String? = null
                val oldCol = oldView.getColumn(columnName)
                val newCol = newView.getColumn(columnName)
                if (oldCol != null) oldComment = oldCol.comment
                if (newCol != null) newComment = newCol.comment
                if (oldComment == null && newComment != null
                    || oldComment != null && newComment != null && oldComment != newComment
                ) {
                    searchPathHelper.outputSearchPath(writer)
                    writer.println()
                    writer.print("COMMENT ON COLUMN ")
                    writer.print(PgDiffUtils.getQuotedName(newView.name))
                    writer.print('.')
                    writer.print(PgDiffUtils.getQuotedName(newCol!!.name))
                    writer.print(" IS ")
                    writer.print(newCol.comment)
                    writer.println(';')
                } else if (oldComment != null
                    && newComment == null
                ) {
                    searchPathHelper.outputSearchPath(writer)
                    writer.println()
                    writer.print("COMMENT ON COLUMN ")
                    writer.print(PgDiffUtils.getQuotedName(newView.name))
                    writer.print('.')
                    writer.print(PgDiffUtils.getQuotedName(oldCol!!.name))
                    writer.println(" IS NULL;")
                }
            }
            if (oldView.ownerTo != null && newView.ownerTo != oldView.ownerTo) {
                writer.println()
                writer.println(
                    "ALTER ${newView.relationKind} "
                            + PgDiffUtils.getQuotedName(newView.name)
                            + " OWNER TO " + newView.ownerTo + ";"
                )
            }
            alterPrivileges(writer, oldView, newView, searchPathHelper)
            alterPrivilegesColumns(writer, oldView, newView, searchPathHelper)
        }
    }

    /**
     * Diffs default values in views.
     *
     * @param writer           writer
     * @param oldView          old view
     * @param newView          new view
     * @param searchPathHelper search path helper
     */
    private fun diffDefaultValues(
        writer: PrintWriter,
        oldView: PgView?, newView: PgView,
        searchPathHelper: SearchPathHelper
    ) {

        // modify defaults that are in old view
        for (oldCol in oldView!!.columns) {
            if (oldCol.defaultValue == null) continue
            val newCol = newView.getColumn(oldCol.name)
            if (newCol != null && newCol.defaultValue != null) {
                if (oldCol.defaultValue != newCol.defaultValue) {
                    searchPathHelper.outputSearchPath(writer)
                    writer.println()
                    writer.print("ALTER TABLE ")
                    writer.print(
                        PgDiffUtils.getQuotedName(newView.name)
                    )
                    writer.print(" ALTER COLUMN ")
                    writer.print(PgDiffUtils.getQuotedName(newCol.name))
                    writer.print(" SET DEFAULT ")
                    writer.print(newCol.defaultValue)
                    writer.println(';')
                }
            } else {
                searchPathHelper.outputSearchPath(writer)
                writer.println()
                writer.print("ALTER TABLE ")
                writer.print(PgDiffUtils.getQuotedName(newView.name))
                writer.print(" ALTER COLUMN ")
                writer.print(PgDiffUtils.getQuotedName(oldCol.name))
                writer.println(" DROP DEFAULT;")
            }
        }

        // add new defaults
        for (newCol in newView.columns) {
            val oldCol = oldView.getColumn(newCol.name)
            if (oldCol != null && oldCol.defaultValue != null
                || newCol.defaultValue == null
            ) {
                continue
            }
            searchPathHelper.outputSearchPath(writer)
            writer.println()
            writer.print("ALTER TABLE ")
            writer.print(PgDiffUtils.getQuotedName(newView.name))
            writer.print(" ALTER COLUMN ")
            writer.print(PgDiffUtils.getQuotedName(newCol.name))
            writer.print(" SET DEFAULT ")
            writer.print(newCol.defaultValue)
            writer.println(';')
        }
    }

    private fun alterPrivileges(
        writer: PrintWriter,
        oldView: PgView?, newView: PgView,
        searchPathHelper: SearchPathHelper
    ) {
        val emptyLinePrinted = false
        for (oldViewPrivilege in oldView!!.privileges) {
            val newViewPrivilege = newView
                .getPrivilege(oldViewPrivilege.roleName)
            if (newViewPrivilege == null) {
                if (!emptyLinePrinted) {
                    writer.println()
                }
                writer.println(
                    "REVOKE ALL ON TABLE "
                            + PgDiffUtils.getQuotedName(oldView.name)
                            + " FROM " + oldViewPrivilege.roleName + ";"
                )
            } else if (!oldViewPrivilege.isSimilar(newViewPrivilege)) {
                if (!emptyLinePrinted) {
                    writer.println()
                }
                writer.println(
                    "REVOKE ALL ON TABLE "
                            + PgDiffUtils.getQuotedName(newView.name)
                            + " FROM " + newViewPrivilege.roleName + ";"
                )
                if ("" != newViewPrivilege.getPrivilegesSQL(true)) {
                    writer.println(
                        "GRANT "
                                + newViewPrivilege.getPrivilegesSQL(true)
                                + " ON TABLE "
                                + PgDiffUtils.getQuotedName(newView.name)
                                + " TO " + newViewPrivilege.roleName
                                + " WITH GRANT OPTION;"
                    )
                }
                if ("" != newViewPrivilege.getPrivilegesSQL(false)) {
                    writer.println(
                        "GRANT "
                                + newViewPrivilege.getPrivilegesSQL(false)
                                + " ON TABLE "
                                + PgDiffUtils.getQuotedName(newView.name)
                                + " TO " + newViewPrivilege.roleName + ";"
                    )
                }
            } // else similar privilege will not be updated
        }
        for (newViewPrivilege in newView.privileges) {
            val oldViewPrivilege = oldView
                .getPrivilege(newViewPrivilege.roleName)
            if (oldViewPrivilege == null) {
                if (!emptyLinePrinted) {
                    writer.println()
                }
                writer.println(
                    "REVOKE ALL ON TABLE "
                            + PgDiffUtils.getQuotedName(newView.name)
                            + " FROM " + newViewPrivilege.roleName + ";"
                )
                if ("" != newViewPrivilege.getPrivilegesSQL(true)) {
                    writer.println(
                        "GRANT "
                                + newViewPrivilege.getPrivilegesSQL(true)
                                + " ON TABLE "
                                + PgDiffUtils.getQuotedName(newView.name)
                                + " TO " + newViewPrivilege.roleName
                                + " WITH GRANT OPTION;"
                    )
                }
                if ("" != newViewPrivilege.getPrivilegesSQL(false)) {
                    writer.println(
                        "GRANT "
                                + newViewPrivilege.getPrivilegesSQL(false)
                                + " ON TABLE "
                                + PgDiffUtils.getQuotedName(newView.name)
                                + " TO " + newViewPrivilege.roleName + ";"
                    )
                }
            }
        }
    }

    private fun alterPrivilegesColumns(
        writer: PrintWriter,
        oldView: PgView?, newView: PgView,
        searchPathHelper: SearchPathHelper
    ) {
        var emptyLinePrinted = false
        for (newColumn in newView.columns) {
            val oldColumn = oldView!!.getColumn(newColumn.name)
            if (oldColumn != null) {
                for (oldColumnPrivilege in oldColumn
                    .privileges) {
                    val newColumnPrivilege = newColumn
                        .getPrivilege(oldColumnPrivilege.roleName)
                    if (newColumnPrivilege == null) {
                        if (!emptyLinePrinted) {
                            emptyLinePrinted = true
                            writer.println()
                        }
                        writer.println(
                            "REVOKE ALL ("
                                    + PgDiffUtils.getQuotedName(newColumn.name)
                                    + ") ON TABLE "
                                    + PgDiffUtils.getQuotedName(newView.name)
                                    + " FROM " + oldColumnPrivilege.roleName
                                    + ";"
                        )
                    }
                }
            }
            if (newColumn != null) {
                for (newColumnPrivilege in newColumn
                    .privileges) {
                    var oldColumnPrivilege: PgColumnPrivilege? = null
                    if (oldColumn != null) {
                        oldColumnPrivilege = oldColumn
                            .getPrivilege(newColumnPrivilege.roleName)
                    }
                    if (!newColumnPrivilege.isSimilar(oldColumnPrivilege)) {
                        if (!emptyLinePrinted) {
                            emptyLinePrinted = true
                            writer.println()
                        }
                        writer.println(
                            "REVOKE ALL ("
                                    + PgDiffUtils.getQuotedName(newColumn.name)
                                    + ") ON TABLE "
                                    + PgDiffUtils.getQuotedName(newView.name)
                                    + " FROM " + newColumnPrivilege.roleName
                                    + ";"
                        )
                        if ("" != newColumnPrivilege.getPrivilegesSQL(
                                true,
                                PgDiffUtils.getQuotedName(newColumn.name)
                            )
                        ) {
                            writer.println(
                                "GRANT "
                                        + newColumnPrivilege.getPrivilegesSQL(
                                    true,
                                    PgDiffUtils.getQuotedName(
                                        newColumn
                                            .name
                                    )
                                )
                                        + " ON TABLE "
                                        + PgDiffUtils.getQuotedName(
                                    newView
                                        .name
                                ) + " TO "
                                        + newColumnPrivilege.roleName
                                        + " WITH GRANT OPTION;"
                            )
                        }
                        if ("" != newColumnPrivilege.getPrivilegesSQL(
                                false,
                                PgDiffUtils.getQuotedName(newColumn.name)
                            )
                        ) {
                            writer.println(
                                "GRANT "
                                        + newColumnPrivilege.getPrivilegesSQL(
                                    false, PgDiffUtils.getQuotedName(
                                        newColumn
                                            .name
                                    )
                                )
                                        + " ON TABLE "
                                        + PgDiffUtils.getQuotedName(
                                    newView
                                        .name
                                ) + " TO "
                                        + newColumnPrivilege.roleName + ";"
                            )
                        }
                    }
                }
            }
        }
    }
}