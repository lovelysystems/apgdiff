package cz.startnet.utils.pgdiff

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import java.nio.charset.Charset

class CLI : CliktCommand(name = "apgdiff") {
    val inCharsetName by option(help = "Input file charset name").default("UTF-8")
    val outCharsetName by option(help = "Input file charset name").default("UTF-8")
    val outFile by option(help = "File to write the diff sql script").file()
    val dropCascade by option(help = "Make objects drops cascading").flag(default = false)
    val addDefaults by option(
        help = "Whether DEFAULT ... should be added in case new" +
                " column has NOT NULL constraint. The default value is dropped later."
    ).flag(default = false)
    val outputIgnoredStatements by option(
        help = "Whether to output information about ignored statements."
    ).flag(default = false)

    val oldDumpFile by argument(help = "Path to the original dump file").file(mustBeReadable = true)
    val newDumpFile by argument(help = "Path to the new dump file").file(mustBeReadable = true)


    override fun run() {
        val arguments = PgDiffOptions(
            isAddDefaults = addDefaults,
            dropCascade = dropCascade
        )
        val dumpOld = oldDumpFile.bufferedReader(Charset.forName(inCharsetName))
        val dumpNew = newDumpFile.bufferedReader(Charset.forName(inCharsetName))
        val charset = Charset.forName(outCharsetName) ?: error("charset $outCharsetName not found")

        val res = PgDiff.createDiff(
            dumpOld, dumpNew,
            outputIgnoredStatements = outputIgnoredStatements,
            options = arguments
        )
        val writer = outFile?.writer(charset) ?: System.out.writer(charset)
        writer.use {
            it.write(res.script)
        }

        val diff = res.diffIgnored()
        if (diff.isNotEmpty()) {
            System.err.println("CAUTION ignored statements differ:")
            diff.forEach(System.err::println)
        }
    }

}

fun main(args: Array<String>) = CLI().main(args)