package cz.startnet.utils.pgdiff

import cz.startnet.utils.pgdiff.loader.PgDumpLoader
import io.github.petertrr.diffutils.diff
import io.github.petertrr.diffutils.patch.Delta
import io.github.petertrr.diffutils.patch.DeltaType
import io.github.petertrr.diffutils.text.DiffRowGenerator
import kotlinx.io.Source

data class PgDiffResult(
    val script: String,
    val ignoredOld: List<String>,
    val ignoredNew: List<String>
) {
    fun diffIgnored(): List<String> {
        DiffRowGenerator
        if (ignoredOld != ignoredNew) {
            val unifiedDiff = generateUnifiedDiff(ignoredOld, ignoredNew)
            return unifiedDiff
        } else {
            return emptyList()
        }
    }

}

fun Delta<String>.toPatchLines(): List<String> {
    return when (type) {
        DeltaType.DELETE -> {
            source.lines.map { "-$it" }
        }

        DeltaType.CHANGE -> {
            source.lines.map { "-$it" } + target.lines.map { "+$it" }
        }

        DeltaType.INSERT -> {
            target.lines.map { "+$it" }
        }

        DeltaType.EQUAL -> {
            source.lines.map { " $it" }
        }

        else -> {
            throw IllegalStateException("Unknown delta type $this")
        }
    }
}

fun generateUnifiedDiff(oldString: List<String>, newString: List<String>): List<String> {
    val patch = diff(oldString, newString)
    return listOf("--- old", "+++ new") + patch.deltas.flatMap { it.toPatchLines() }
}

class PgDiff(
    private val options: PgDiffOptions = PgDiffOptions()
) {

    /**
     * Creates diff on the two database schemas.
     */
    fun createDiff(
        oldReader: Source,
        newReader: Source
    ): PgDiffResult {
        val oldDatabase = PgDumpLoader.loadDatabaseSchema(
            oldReader
        )
        val newDatabase = PgDumpLoader.loadDatabaseSchema(
            newReader
        )
        //val stream = ByteArrayOutputStream()
        //val writer = DiffWriter(stream, options)
        val builder = StringBuilder()
        val diffDBs = PgDiffDatabases(builder, options, oldDatabase, newDatabase)
        diffDBs()
        // writer.close()
        return PgDiffResult(
            builder.toString(), // TODO: charset
            //stream.toString(options.outCharsetName),
            ignoredOld = oldDatabase.ignoredStatements,
            ignoredNew = newDatabase.ignoredStatements
        )

    }
}

