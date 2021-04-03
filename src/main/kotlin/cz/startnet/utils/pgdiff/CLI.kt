package cz.startnet.utils.pgdiff

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.nio.charset.Charset

class CLI : CliktCommand(name = "apgdiff") {
    val inCharsetName by option(help = "Input file charset name").default("UTF-8")
    val outCharsetName by option(help = "Input file charset name").default("UTF-8")
    val isAddDefaults by option(
        help = "Whether DEFAULT ... should be added in case new" +
                " column has NOT NULL constraint. The default value is dropped later."
    ).flag(default = false)
    val isOutputIgnoredStatements by option(
        help = "Whether to output information about ignored statements."
    ).flag(default = false)

    val oldDumpFile by argument(help = "Path to the original dump file").file(mustBeReadable = true)
    val newDumpFile by argument(help = "Path to the new dump file").file(mustBeReadable = true)


    override fun run() {
        val arguments = PgDiffOptions(
            isOutputIgnoredStatements = isOutputIgnoredStatements,
            isAddDefaults = isAddDefaults
        )
        val dumpOld = oldDumpFile.bufferedReader(Charset.forName(inCharsetName))
        val dumpNew = newDumpFile.bufferedReader(Charset.forName(inCharsetName))
        val writer = PrintWriter(OutputStreamWriter(System.out, outCharsetName))
        PgDiff.createDiff(writer, arguments, dumpOld, dumpNew)
        writer.close()
    }

}

fun main(args: Array<String>) = CLI().main(args)