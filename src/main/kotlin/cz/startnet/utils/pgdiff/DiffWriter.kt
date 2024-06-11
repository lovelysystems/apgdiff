package cz.startnet.utils.pgdiff

import java.io.OutputStream
import kotlin.text.StringBuilder
import java.nio.charset.Charset

class DiffWriter(outputStream: OutputStream, val arguments: PgDiffOptions) {
    //val builder = StringBuilder(outputStream, false, Charset.forName(arguments.outCharsetName))
    val builder = StringBuilder()
//    fun printStmt(vararg parts: String) {
//        val stmt = parts.joinToString(" ", postfix = ";") { it.trimStart() }
//        builder.println(stmt)
//    }

    fun close() {
        TODO("Not yet implemented")
    }

    fun flush() {
        TODO("Not yet implemented")
    }
}