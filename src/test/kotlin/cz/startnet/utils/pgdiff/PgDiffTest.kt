package cz.startnet.utils.pgdiff

import io.kotest.matchers.string.shouldBeEmpty
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
        diff.trim { it <= ' ' }.shouldBeEmpty()
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
        diff.trim { it <= ' ' }.shouldBeEmpty()
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
    }
}
