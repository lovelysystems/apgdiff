package cz.startnet.utils.pgdiff

import cz.startnet.utils.pgdiff.schema.PgColumnUtils
import cz.startnet.utils.pgdiff.schema.PgSchema
import cz.startnet.utils.pgdiff.schema.PgType
import cz.startnet.utils.pgdiff.schema.PgTypeColumn

/**
 * Diffs types.
 */
object PgDiffTypes {
    /**
     * Outputs statements for altering types.
     *
     * @param writer           writer the output should be written to
     * @param arguments        object containing arguments settings
     * @param oldSchema        original schema
     * @param newSchema        new schema
     */
    fun alterTypes(
        writer: StringBuilder,
        arguments: PgDiffOptions, oldSchema: PgSchema?,
        newSchema: PgSchema
    ) {
        for (newType in newSchema.types) {
            val oldType = oldSchema?.getType(newType.name) ?: continue
            updateTypeColumns(
                writer, arguments, oldType, newType
            )
            if (newType.owner != oldType.owner) {
                writer.appendLine(newType.ownerSQL)
            }
            if (newType.comment != oldType.comment) {
                writer.appendLine(newType.commentSQL)
            }
        }
    }

    /**
     * Adds statements for creation of new columns to the list of statements.
     *
     * @param statements          list of statements
     * @param arguments           object containing arguments settings
     * @param oldType            original type
     * @param newType            new type
     * @param dropDefaultsColumns list for storing columns for which default
     * value should be dropped
     */
    private fun addCreateTypeColumns(
        statements: MutableList<String>,
        arguments: PgDiffOptions, oldType: PgType?,
        newType: PgType, dropDefaultsColumns: MutableList<PgTypeColumn>
    ) {
        for (column in newType.columns) {
            if (!oldType!!.containsColumn(column.name)) {
                statements.add(
                    "\tADD ATTRIBUTE "
                            + column.getFullDefinition(arguments.isAddDefaults)
                )
                if (arguments.isAddDefaults && !column.nullValue
                    && (column.defaultValue.isNullOrEmpty())
                ) {
                    dropDefaultsColumns.add(column)
                }
            }
        }
    }

    /**
     * Adds statements for removal of columns to the list of statements.
     *
     * @param statements list of statements
     * @param oldType   original type
     * @param newType   new type
     */
    private fun addDropTypeColumns(
        statements: MutableList<String>,
        oldType: PgType?, newType: PgType?
    ) {
        for (column in oldType!!.columns) {
            if (!newType!!.containsColumn(column.name)) {
                statements.add(
                    "\tDROP ATTRIBUTE "
                            + PgDiffUtils.getQuotedName(column.name)
                )
            }
        }
    }

    /**
     * Adds statements for modification of columns to the list of statements.
     *
     * @param statements          list of statements
     * @param arguments           object containing arguments settings
     * @param oldType            original type
     * @param newType            new type
     * @param dropDefaultsColumns list for storing columns for which default
     * value should be dropped
     */
    private fun addModifyTypeColumns(
        statements: MutableList<String>,
        arguments: PgDiffOptions, oldType: PgType?,
        newType: PgType?, dropDefaultsColumns: MutableList<PgTypeColumn>
    ) {
        for (newColumn in newType!!.columns) {
            if (!oldType!!.containsColumn(newColumn.name)) {
                continue
            }
            val oldColumn = oldType.getColumn(newColumn.name)!!
            val newColumnName = PgDiffUtils.getQuotedName(newColumn.name)
            if (oldColumn.type != newColumn.type) {
                statements.add(
                    "\tALTER ATTRIBUTE $newColumnName TYPE ${newColumn.type}"
                            + " /* TYPE change - table: ${newType.name} original: ${oldColumn.type} new: ${newColumn.type}  */"
                )
            }
            val oldDefault = oldColumn.defaultValue.orEmpty()
            val newDefault = newColumn.defaultValue.orEmpty()
            if (oldDefault != newDefault) {
                if (newDefault.isEmpty()) {
                    statements.add(
                        "\tALTER ATTRIBUTE " + newColumnName
                                + " DROP DEFAULT"
                    )
                } else {
                    statements.add(
                        "\tALTER ATTRIBUTE " + newColumnName
                                + " SET DEFAULT " + newDefault
                    )
                }
            }
            if (oldColumn.nullValue != newColumn.nullValue) {
                if (newColumn.nullValue) {
                    statements.add(
                        "\tALTER ATTRIBUTE " + newColumnName
                                + " DROP NOT NULL"
                    )
                } else {
                    if (arguments.isAddDefaults) {
                        val defaultValue = PgColumnUtils.getDefaultValue(
                            requireNotNull(newColumn.type)
                        )
                        if (defaultValue != null) {
                            statements.add(
                                "\tALTER ATTRIBUTE " + newColumnName
                                        + " SET DEFAULT " + defaultValue
                            )
                            dropDefaultsColumns.add(newColumn)
                        }
                    }
                    statements.add(
                        "\tALTER ATTRIBUTE " + newColumnName
                                + " SET NOT NULL"
                    )
                }
            }
        }
    }

    /**
     * Outputs statements for creation of new types.
     *
     * @param writer           writer the output should be written to
     * @param oldSchema        original schema
     * @param newSchema        new schema
     */
    fun createTypes(
        writer: StringBuilder,
        oldSchema: PgSchema?, newSchema: PgSchema?
    ) {
        for (type in newSchema!!.types) {
            if (oldSchema == null
                || !oldSchema.containsType(type.name)
            ) {
                writer.println()
                writer.appendLine(type.creationSQL)
            }
        }
    }

    /**
     * Outputs statements for addition, removal and modifications of type
     * columns.
     *
     * @param writer           writer the output should be written to
     * @param arguments        object containing arguments settings
     * @param oldType         original type
     * @param newType         new type
     */
    private fun updateTypeColumns(
        writer: StringBuilder,
        arguments: PgDiffOptions, oldType: PgType?,
        newType: PgType
    ) {
        val statements: MutableList<String> = ArrayList()
        val dropDefaultsColumns: MutableList<PgTypeColumn> = ArrayList()
        addDropTypeColumns(statements, oldType, newType)
        addCreateTypeColumns(
            statements, arguments, oldType, newType, dropDefaultsColumns
        )
        addModifyTypeColumns(
            statements, arguments, oldType, newType, dropDefaultsColumns
        )
        if (statements.isNotEmpty()) {
            val quotedTypeName = PgDiffUtils.getQuotedName(newType.name)
            writer.println()
            writer.appendLine("ALTER TYPE $quotedTypeName")
            for (i in statements.indices) {
                writer.append(statements[i])
                writer.appendLine(if (i + 1 < statements.size) "," else ";")
            }
            if (dropDefaultsColumns.isNotEmpty()) {
                writer.println()
                writer.appendLine("ALTER TYPE $quotedTypeName")
                for (i in dropDefaultsColumns.indices) {
                    writer.append("\tALTER ATTRIBUTE ")
                    writer.append(
                        PgDiffUtils.getQuotedName(
                            dropDefaultsColumns[i].name
                        )
                    )
                    writer.append(" DROP DEFAULT")
                    writer.appendLine(
                        if (i + 1 < dropDefaultsColumns.size) "," else ";"
                    )
                }
            }
        }
    }
}