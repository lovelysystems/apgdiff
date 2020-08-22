/**
 * Copyright 2006 StartNet s.r.o.
 *
 * Distributed under MIT license
 */
package cz.startnet.utils.pgdiff.loader

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

/**
 * Tests for PgDiffLoader class.
 *
 * @author fordfrog
 */
@RunWith(value = Parameterized::class)
class PgDumpLoaderTest
/**
 * Creates a new instance of PgDumpLoaderTest.
 *
 * @param fileIndex [.fileIndex]
 */(
    /**
     * Index of the file that should be tested.
     */
    private val fileIndex: Int
) {
    /**
     * Runs single test.
     */
    @Test(timeout = 1000)
    fun loadSchema() {
        PgDumpLoader.loadDatabaseSchema(
            javaClass.getResourceAsStream("schema_$fileIndex.sql"),
            "UTF-8", false, false, false
        )
    }

    companion object {
        /**
         * Provides parameters for running the tests.
         *
         * @return parameters for the tests
         */
        @JvmStatic
        @Parameterized.Parameters(name = "schema_{0}.sql")
        fun parameters(): Collection<*> {
            return (1..18).toList()
        }
    }
}