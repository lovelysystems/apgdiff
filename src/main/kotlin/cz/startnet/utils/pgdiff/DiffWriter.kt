package cz.startnet.utils.pgdiff

import java.io.OutputStream
import java.io.PrintWriter
import java.nio.charset.Charset

class DiffWriter(outputStream: OutputStream, val arguments: PgDiffOptions) :
    PrintWriter(outputStream, false, Charset.forName(arguments.outCharsetName)) {

    fun printStmt(vararg parts: String) {
        val stmt = parts.joinToString(" ", postfix = ";") { it.trimStart() }
        println(stmt)
    }
}