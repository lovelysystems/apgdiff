/**
 * Copyright 2006 StartNet s.r.o.
 *
 * Distributed under MIT license
 */
package cz.startnet.utils.pgdiff

import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.io.UnsupportedEncodingException

/**
 * Compares two PostgreSQL dumps and outputs information about differences in
 * the database schemas.
 *
 * @author fordfrog
 */
object Main {
    /**
     * APgDiff main method.
     *
     * @param args the command line arguments
     *
     * @throws UnsupportedEncodingException Thrown if unsupported output
     * encoding has been encountered.
     */
    @Throws(UnsupportedEncodingException::class)
    @JvmStatic
    fun main(args: Array<String>) {
        val writer = PrintWriter(System.out, true)
        val arguments = PgDiffArguments()
        if (arguments.parse(writer, args)) {
            val encodedWriter = PrintWriter(
                OutputStreamWriter(
                    System.out, arguments.outCharsetName
                )
            )
            PgDiff.createDiff(encodedWriter, arguments)
            encodedWriter.close()
        }
        writer.close()
    }
}