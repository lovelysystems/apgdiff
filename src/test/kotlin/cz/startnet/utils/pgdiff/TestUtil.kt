package cz.startnet.utils.pgdiff

import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintWriter
import java.util.stream.Stream
import kotlin.streams.asStream

val testFileDir = File("src/test/resources/pgdiff_test_files")

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

    override fun provideArguments(context: ExtensionContext): Stream<out Arguments>? {

        val names = testFileDir.list()!!.map {
            originalPattern.matchEntire(it)?.groups?.get(1)?.value
        }.filterNotNull().sorted()

        return names.asSequence().map {
            Arguments.of(SQLDiffTestFiles(it))
        }.asStream()

    }
}

fun PgDiff.createDiff(oldDump: String, newDump: String): String {
    val diffInput = ByteArrayOutputStream()
    val writer = PrintWriter(diffInput)
    val arguments = PgDiffArguments()
    PgDiff.createDiff(
        writer, arguments,
        oldDump.byteInputStream(),
        newDump.byteInputStream()
    )
    writer.close()
    return diffInput.toString()
}

