package cz.startnet.utils.pgdiff

import cz.startnet.utils.pgdiff.schema.PgFunction
import cz.startnet.utils.pgdiff.schema.PgSchema
import kotlin.text.StringBuilder

object PgDiffFunctions {
    /**
     * Outputs statements for new or modified functions.
     *
     * @param writer           writer the output should be written to
     * @param arguments        object containing arguments settings
     * @param oldSchema        original schema
     * @param newSchema        new schema
     */
    fun createFunctions(
        writer: StringBuilder,
        arguments: PgDiffOptions, oldSchema: PgSchema?,
        newSchema: PgSchema
    ) {
        // Add new functions and replace modified functions
        for (newFunction in newSchema.functions) {
            val oldFunction: PgFunction? = oldSchema?.getFunction(newFunction.signature)

            if (oldFunction == null || !newFunction.equals(
                    oldFunction, arguments.isIgnoreFunctionWhitespace
                )
            ) {
                writer.println()
                // drop the function if args differ since the signature cannot be changed via replace
                val toDrop = (oldFunction != null && oldFunction.arguments != newFunction.arguments)
                if (toDrop) {
                    writer.appendLine(newFunction.dropSQL)
                }
                writer.appendLine(newFunction.creationSQL(!toDrop))
            }
        }
    }

    /**
     * Outputs statements for dropping of functions that exist no more.
     *
     * @param writer           writer the output should be written to
     * @param oldSchema        original schema
     * @param newSchema        new schema
     */
    fun dropFunctions(
        writer: StringBuilder,
        oldSchema: PgSchema?, newSchema: PgSchema
    ) {
        if (oldSchema == null) {
            return
        }

        // Drop functions that exist no more
        for (oldFunction in oldSchema.functions) {
            if (!newSchema.containsFunction(oldFunction.signature)) {
                writer.println()
                writer.appendLine(oldFunction.dropSQL)
            }
        }
    }

    /**
     * Outputs statements for function comments that have changed.
     *
     * @param writer           writer
     * @param oldSchema        old schema
     * @param newSchema        new schema
     */
    fun alterComments(
        writer: StringBuilder,
        oldSchema: PgSchema?, newSchema: PgSchema
    ) {
        if (oldSchema == null) {
            return
        }
        for (oldFunction in oldSchema.functions) {
            val newFunction = newSchema.getFunction(oldFunction.signature) ?: continue
            if (oldFunction.comment == null
                && newFunction.comment != null
                || oldFunction.comment != null && newFunction.comment != null && oldFunction.comment != newFunction.comment
            ) {

                writer.println()
                writer.appendLine("COMMENT ON FUNCTION ${newFunction.signatureSQL} IS ${newFunction.comment};")
            } else if (oldFunction.comment != null
                && newFunction.comment == null
            ) {

                writer.println()
                writer.appendLine("COMMENT ON FUNCTION ${newFunction.signatureSQL} IS NULL;")
            }
        }
    }
}