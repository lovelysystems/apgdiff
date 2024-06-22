package cz.startnet.utils.pgdiff

import com.github.ajalt.clikt.completion.CompletionCandidates
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.arguments.ProcessedArgument
import com.github.ajalt.clikt.parameters.arguments.RawArgument
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.convert
import com.github.ajalt.clikt.parameters.options.*
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.writeString

private fun pathType(context: Context, fileOkay: Boolean, folderOkay: Boolean): String = when {
    fileOkay && !folderOkay -> context.localization.pathTypeFile()
    !fileOkay && folderOkay -> context.localization.pathTypeDirectory()
    else -> context.localization.pathTypeOther()
}

private fun convertToPath(
    path: String,
    mustExist: Boolean,
    canBeFile: Boolean,
    canBeFolder: Boolean,
    context: Context,
    fail: (String) -> Unit,
): Path {
    val name = pathType(context, canBeFile, canBeFolder)
    return with(context.localization) {
        val p = Path(path)
        val exists = SystemFileSystem.exists(p)
        if (exists) {
            SystemFileSystem.resolve(Path(path))
        } else {
            if (mustExist) {
                fail(pathDoesNotExist(name, p.toString()))
            }
            p
        }

    }
}

fun RawArgument.path(
    mustExist: Boolean = false,
    canBeFile: Boolean = true,
    canBeDir: Boolean = true,
): ProcessedArgument<Path, Path> {
    return convert(CompletionCandidates.Path) { str ->
        convertToPath(
            path = str,
            mustExist = mustExist,
            canBeFile = canBeFile,
            canBeFolder = canBeDir,
            context = context
        ) { fail(it) }
    }
}


fun RawOption.path(
    mustExist: Boolean = false,
    canBeFile: Boolean = true,
    canBeDir: Boolean = true,
): NullableOption<Path, Path> {
    return convert({ localization.pathMetavar() }, CompletionCandidates.Path) { str ->
        convertToPath(
            path = str,
            mustExist = mustExist,
            canBeFile = canBeFile,
            canBeFolder = canBeDir,
            context = context
        ) { fail(it) }
    }
}


class CLI : CliktCommand(name = "apgdiff") {
    private val outFile by option(help = "File to write the diff sql script").path(mustExist = false)
    private val dropCascade by option(help = "Make objects drops cascading").flag(default = false)
    private val addDefaults by option(
        help = "Whether DEFAULT ... should be added in case new" +
                " column has NOT NULL constraint. The default value is dropped later."
    ).flag(default = false)
    private val outputIgnoredStatements by option(
        help = "Whether to output information about ignored statements."
    ).flag(default = false)

    private val oldDumpFile by argument(help = "Path to the original dump file").path(mustExist = true)
    private val newDumpFile by argument(help = "Path to the new dump file").path(mustExist = true)

    private val schemas: List<String> by option(
        "-n",
        "--schema",
        help = "diff the specified schema(s) only. diffs all if not given",
        metavar = "REGEX_PATTERN"
    ).multiple()

    private val excludeSchemas: List<String> by option(
        "-N",
        "--exclude-schema",
        help = "do NOT diff the specified schema(s)",
        metavar = "REGEX_PATTERN"
    ).multiple()


    override fun run() {
        val arguments = PgDiffOptions(
            isAddDefaults = addDefaults,
            dropCascade = dropCascade,
            outputIgnoredStatements = outputIgnoredStatements,
            excludeSchemas = excludeSchemas,
            schemas = schemas
        )

        val oldSource = SystemFileSystem.source(oldDumpFile).buffered()
        val newSource = SystemFileSystem.source(newDumpFile).buffered()

        val res = PgDiff(arguments).createDiff(oldSource, newSource)

        if (outFile != null) {
            val f = SystemFileSystem.sink(outFile!!).buffered()
            f.writeString(res.script)
            f.close()
        } else {
            echo(res.script, trailingNewline = false)
        }

        val diff = res.diffIgnored()
        if (diff.isNotEmpty()) {
            echo("CAUTION ignored statements differ:", err = true)
            diff.forEach {
                echo(it, err = true)
            }
        }
    }

}

fun main(args: Array<String>) = CLI().main(args)