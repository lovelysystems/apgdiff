/**
 * Copyright 2010 StartNet s.r.o.
 */
package cz.startnet.utils.pgdiff

import kotlin.text.StringBuilder

/**
 * Helps to output search path only if it was not output yet.
 *
 * @author fordfrog
 */
class SearchPathHelper
/**
 * Creates new instance of SearchPathHelper.
 *
 * @param searchPath [.searchPath]
 */(
    /**
     * Statement to output.
     */
    private val searchPath: String?
) {
    /**
     * Flag determining whether the statement was already output.
     */
    private var wasOutput = false

    /**
     * Outputs search path if it was not output yet.
     *
     * @param writer writer
     */
    fun outputSearchPath(writer: StringBuilder) {
        if (!wasOutput && !searchPath.isNullOrEmpty()) {
            writer.println()
            writer.appendLine(searchPath)
            wasOutput = true
        }
    }
}
