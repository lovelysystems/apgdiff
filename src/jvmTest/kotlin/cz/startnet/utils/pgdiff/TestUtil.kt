package cz.startnet.utils.pgdiff

import io.kotest.matchers.string.shouldBeBlank
import io.kotest.matchers.string.shouldBeEmpty
import kotlinx.io.asSource
import kotlinx.io.buffered
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import java.io.File
import java.util.stream.Stream
import kotlin.streams.asStream

val testFileDir = File("src/jvmTest/resources/pgdiff_test_files")

data class SQLDiffTestFiles(val name: String) {

    override fun toString(): String = name

    val old: File
        get() = testFileDir.resolve("${name}_original.sql")

    val new: File
        get() = testFileDir.resolve("${name}_new.sql")

    val diff: File
        get() = testFileDir.resolve("${name}_diff.sql")

}

class SQLDiffFilesArgumentsProvider : ArgumentsProvider {


    private val originalPattern = Regex("^(.*)_original.sql")

    override fun provideArguments(context: ExtensionContext): Stream<out Arguments> {

        val names = testFileDir.list()!!.mapNotNull {
            originalPattern.matchEntire(it)?.groups?.get(1)?.value
        }.sorted()

        return names.asSequence().map {
            Arguments.of(SQLDiffTestFiles(it))
        }.asStream()

    }
}

fun PgDiff.createDiff(oldDump: String, newDump: String = oldDump): PgDiffResult {
    return createDiff(
        oldDump.byteInputStream().asSource().buffered(), //  .bufferedReader(),
        newDump.byteInputStream().asSource().buffered()//.bufferedReader()
    )
}

fun PgDiffResult.shouldHaveNoDiff() {
    script.shouldBeBlank()
    diffIgnored().joinToString("\n").shouldBeEmpty()
}
