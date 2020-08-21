/**
 * Copyright 2006 StartNet s.r.o.
 *
 * Distributed under MIT license
 */
package cz.startnet.utils.pgdiff.loader

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.util.*

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
        @Parameterized.Parameters
        fun parameters(): Collection<*> {
            return Arrays.asList(
                *arrayOf(
                    arrayOf<Any>(1),
                    arrayOf<Any>(2),
                    arrayOf<Any>(3),
                    arrayOf<Any>(4),
                    arrayOf<Any>(5),
                    arrayOf<Any>(6),
                    arrayOf<Any>(7),
                    arrayOf<Any>(8),
                    arrayOf<Any>(9),
                    arrayOf<Any>(10),
                    arrayOf<Any>(11),
                    arrayOf<Any>(12),
                    arrayOf<Any>(13),
                    arrayOf<Any>(14),
                    arrayOf<Any>(15),
                    arrayOf<Any>(16),
                    arrayOf<Any>(18)
                )
            )
        }
    }
}