/**
 * Copyright 2006 StartNet s.r.o.
 *
 * Distributed under MIT license
 */
package cz.startnet.utils.pgdiff

import com.github.difflib.DiffUtils
import com.github.difflib.UnifiedDiffUtils
import cz.startnet.utils.pgdiff.loader.PgDumpLoader
import java.io.BufferedReader
import java.io.ByteArrayOutputStream


data class PgDiffResult(
    val script: String,
    val ignoredOld: List<String>,
    val ignoredNew: List<String>
) {
    fun diffIgnored(): List<String> {
        if (ignoredOld != ignoredNew) {
            val d = DiffUtils.diff(ignoredOld, ignoredNew)
            val unifiedDiff = UnifiedDiffUtils.generateUnifiedDiff(
                "old", "new", ignoredOld, d, 0
            )
            return unifiedDiff
        } else {
            return emptyList()
        }
    }

}


class PgDiff(
    private val options: PgDiffOptions = PgDiffOptions()
) {

    /**
     * Creates diff on the two database schemas.
     */
    fun createDiff(
        oldReader: BufferedReader,
        newReader: BufferedReader
    ): PgDiffResult {
        val oldDatabase = PgDumpLoader.loadDatabaseSchema(
            oldReader
        )
        val newDatabase = PgDumpLoader.loadDatabaseSchema(
            newReader
        )
        val stream = ByteArrayOutputStream()
        val writer = DiffWriter(stream, options)
        val diffDBs = PgDiffDatabases(writer, options, oldDatabase, newDatabase)
        diffDBs()
        writer.close()
        return PgDiffResult(
            stream.toString(options.outCharsetName),
            ignoredOld = oldDatabase.ignoredStatements,
            ignoredNew = newDatabase.ignoredStatements
        )

    }
}

