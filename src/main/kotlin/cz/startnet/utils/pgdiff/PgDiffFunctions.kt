/**
 * Copyright 2006 StartNet s.r.o.
 *
 * Distributed under MIT license
 */
package cz.startnet.utils.pgdiff

import cz.startnet.utils.pgdiff.schema.PgFunction
import cz.startnet.utils.pgdiff.schema.PgSchema
import java.io.PrintWriter

/**
 * Diffs functions.
 *
 * @author fordfrog
 */
object PgDiffFunctions {
    /**
     * Outputs statements for new or modified functions.
     *
     * @param writer           writer the output should be written to
     * @param arguments        object containing arguments settings
     * @param oldSchema        original schema
     * @param newSchema        new schema
     * @param searchPathHelper search path helper
     */
    fun createFunctions(
        writer: PrintWriter,
        arguments: PgDiffArguments, oldSchema: PgSchema?,
        newSchema: PgSchema?, searchPathHelper: SearchPathHelper
    ) {
        // Add new functions and replace modified functions
        for (newFunction in newSchema!!.functions) {
            val oldFunction: PgFunction?
            oldFunction = oldSchema?.getFunction(newFunction.signature)
            if (oldFunction == null || !newFunction.equals(
                    oldFunction, arguments.isIgnoreFunctionWhitespace
                )
            ) {
                searchPathHelper.outputSearchPath(writer)
                writer.println()
                writer.println(newFunction.creationSQL)
            }
        }
    }

    /**
     * Outputs statements for dropping of functions that exist no more.
     *
     * @param writer           writer the output should be written to
     * @param arguments        object containing arguments settings
     * @param oldSchema        original schema
     * @param newSchema        new schema
     * @param searchPathHelper search path helper
     */
    fun dropFunctions(
        writer: PrintWriter,
        oldSchema: PgSchema?, newSchema: PgSchema?,
        searchPathHelper: SearchPathHelper
    ) {
        if (oldSchema == null) {
            return
        }

        // Drop functions that exist no more
        for (oldFunction in oldSchema.functions) {
            if (!newSchema!!.containsFunction(oldFunction.signature)) {
                searchPathHelper.outputSearchPath(writer)
                writer.println()
                writer.println(oldFunction.dropSQL)
            }
        }
    }

    /**
     * Outputs statements for function comments that have changed.
     *
     * @param writer           writer
     * @param oldSchema        old schema
     * @param newSchema        new schema
     * @param searchPathHelper search path helper
     */
    fun alterComments(
        writer: PrintWriter,
        oldSchema: PgSchema?, newSchema: PgSchema?,
        searchPathHelper: SearchPathHelper
    ) {
        if (oldSchema == null) {
            return
        }
        for (oldfunction in oldSchema.functions) {
            val newFunction = newSchema!!.getFunction(oldfunction.signature) ?: continue
            if (oldfunction.comment == null
                && newFunction.comment != null
                || oldfunction.comment != null && newFunction.comment != null && oldfunction.comment != newFunction.comment
            ) {
                searchPathHelper.outputSearchPath(writer)
                writer.println()
                writer.print("COMMENT ON FUNCTION ")
                writer.print(PgDiffUtils.getQuotedName(newFunction.name))
                writer.print('(')
                var addComma = false
                for (argument in newFunction.arguments) {
                    if (addComma) {
                        writer.print(", ")
                    } else {
                        addComma = true
                    }
                    writer.print(argument.getDeclaration(false))
                }
                writer.print(") IS ")
                writer.print(newFunction.comment)
                writer.println(';')
            } else if (oldfunction.comment != null
                && newFunction.comment == null
            ) {
                searchPathHelper.outputSearchPath(writer)
                writer.println()
                writer.print("COMMENT ON FUNCTION ")
                writer.print(PgDiffUtils.getQuotedName(newFunction.name))
                writer.print('(')
                var addComma = false
                for (argument in newFunction.arguments) {
                    if (addComma) {
                        writer.print(", ")
                    } else {
                        addComma = true
                    }
                    writer.print(argument.getDeclaration(false))
                }
                writer.println(") IS NULL;")
            }
        }
    }
}