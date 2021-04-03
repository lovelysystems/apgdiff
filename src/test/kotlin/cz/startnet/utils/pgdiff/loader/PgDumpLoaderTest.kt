package cz.startnet.utils.pgdiff.loader

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.io.File

class PgDumpLoaderTest {

    fun sqlFiles() = javaClass.getResourceAsStream("/loader_test_files")?.let {
        it.reader().readLines().filter { it.endsWith(".sql") }
    } ?: error("directory for test files not found")

    @ParameterizedTest
    @MethodSource("sqlFiles")
    fun loadSchema(sqlFile: File) {
        javaClass.getResourceAsStream("/loader_test_files/$sqlFile").use {
            PgDumpLoader.loadDatabaseSchema(
                it.bufferedReader(), true, false, false
            )
        }
    }
}
