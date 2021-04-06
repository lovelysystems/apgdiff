package cz.startnet.utils.pgdiff

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldBeBlank
import io.kotest.matchers.string.shouldBeEmpty
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ArgumentsSource

class PgDiffTest {

    /**
     * Runs single test on original schema.
     *
     * expected diff.
     */
    @ParameterizedTest
    @ArgumentsSource(SQLDiffFilesArgumentsProvider::class)
    fun runDiffSameOriginal(testFiles: SQLDiffTestFiles) {
        val dump = testFiles.old.readText()
        val diff = PgDiff.createDiff(dump, dump)
        diff.script.shouldBeBlank()
        diff.diffIgnored().shouldBeEmpty()
    }

    /**
     * Runs single test on new (target) schema.
     *
     * expected diff.
     */
    @ParameterizedTest
    @ArgumentsSource(SQLDiffFilesArgumentsProvider::class)
    fun runDiffSameNew(testFiles: SQLDiffTestFiles) {
        val dump = testFiles.new.readText()
        val diff = PgDiff.createDiff(dump, dump)
        diff.script.shouldBeBlank()
        diff.diffIgnored().shouldBeEmpty()
    }

    /**
     * Runs a diff, but does not assert the contents of the diff
     *
     * expected diff.
     */
    @ParameterizedTest
    @ArgumentsSource(SQLDiffFilesArgumentsProvider::class)
    fun runDiff(testFiles: SQLDiffTestFiles) {
        val old = testFiles.old.readText()
        val new = testFiles.new.readText()
        val diff = PgDiff.createDiff(old, new)
        diff.diffIgnored().joinToString("\n").shouldBeEmpty()
    }

    @Test
    fun testIgnoredDiffer() {
        val old = "CREATE SERVER myserver FOREIGN DATA WRAPPER postgres_fdw OPTIONS (dbname 'foodb');"
        val new = "CREATE SERVER myserver FOREIGN DATA WRAPPER postgres_fdw OPTIONS (dbname 'bardb');"
        val diff = PgDiff.createDiff(old, new)
        diff.script.shouldBeBlank()
        diff.diffIgnored().joinToString("\n") shouldBe """
            --- old
            +++ new
            @@ -1,1 +1,1 @@
            -CREATE SERVER myserver FOREIGN DATA WRAPPER postgres_fdw OPTIONS (dbname 'foodb');
            +CREATE SERVER myserver FOREIGN DATA WRAPPER postgres_fdw OPTIONS (dbname 'bardb');
            """.trimIndent()
    }
}
