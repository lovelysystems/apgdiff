package cz.startnet.utils.pgdiff.loader

import kotlinx.io.asSource
import kotlinx.io.buffered
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.io.File

class PgDumpLoaderTest {

    private fun sqlFiles() = javaClass.getResourceAsStream("/loader_test_files")?.let { testFiles ->
        testFiles.reader().readLines().filter { filename -> filename.endsWith(".sql") }
    } ?: error("directory for test files not found")

    @ParameterizedTest
    @MethodSource("sqlFiles")
    fun loadSchema(sqlFile: File) {
        javaClass.getResourceAsStream("/loader_test_files/$sqlFile").use {
            PgDumpLoader.loadDatabaseSchema(
                it.asSource().buffered()
            )
        }
    }
}
